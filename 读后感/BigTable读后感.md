# BigTable读后感

[TOC]

## 一、BigTable是什么

​	BigTable是一个分布式存储系统，它可以支持拓展到很大尺寸的数据，它同时为不同的需求：无论是从数据的规模，还是对延迟敏感任务数据服务，都提供了一个灵活而高性能的解决方案，使得系统拥有高适用性、高容错性、高可用性以及可拓展性。

## 二、数据模型

​	一个BigTable是一个稀疏的、分布的、永久的多维排序图，其采用行键（row key）、列族（column families）和时间戳（timestamps）进行索引。

- 行键（row key）可以由任意不超过64KB的字符串组成，对每个行键下所包含的数据的读或是写都是一个原子操作。BigTable在行键上根据字典序对数据进行维护，同时还可动态划分出行区间，每一个行区间称为一个Tablet，它是负载均衡和数据分发的基本单位，其可以存储在不同的子表服务器上做负载均衡。一般来说，字典序相接近的两个行大概率是属于同一领域的，这样其被划分到同一个服务器的概率就比较大，这样存取的效率也更高
- 列家族（column families）：通常在实际的数据表中，我们通常会有很多的列。在传统的关系型数据库中，我们只以列为粒度。而BigTable中则把列划分为了一个一个的列家族，这样列键就可以这样来命名：family:qualifier，这样形成的列关键字可以来存储一些相似的数据，从而达到在容纳同样数量的列数同时，也将相同类型的列聚集为一个族来统一管理，甚至可以将同一个列家族中的数据一起进行压缩，方便了数据的管理，同时也提高了数据存储的灵活度。
- 时间戳（Timestamps）：BigTable中的每个单元格中，通常都包含着相同数据的多个版本，这些版本采用时间戳进行索引，时间戳可以以微秒计算，以64位整数分形式进行存储，也可以由客户应用进行直接分配。需要避免冲突的应用必须生成唯一的时间戳。一个单元格的不同版本是根据时间戳降序的顺序进行存储的，这样的话最新的版本可以被优先读取。

​	Bigtable中每一个数据单元格式如下：

​	(row:string, column:string, time:int64) -> string

![image-20201126224412511](http://image.hihia.top/Screenshot/image-20201126224412511.png)

这里我可以举一个例子，如图所示：我可以存乔布斯不同时期的照片，这也相当于搜索引擎抓到不同页面的不同版本，同时也可以存乔布斯不同时期的身高体重。而这里的身高体重可以组成一个关于body的column families。这上面这个图就是逻辑视图，那么我们怎么转换成实际的物理存储呢？我们可以存到一个<Key, Value>Table中，这里的每一条数据就可以是这样的：Steve;Bod:Height;2011 => 6'2''。

## 三、设计概括

### 如何保存一个文件表？

当我们要在众多文件中进行查找时，我们是可以在文件中找到key，按照key来进行排序。之后我们就可以利用诸如二分查找来在表中查找文件了。

![image-20201206185518245](http://image.hihia.top/Screenshot/image-20201206185518245.png)

### 如何保存一个很大的表

这里我们就可以把一个大表拆成很多小表，让每个小表同之前一样，里面存放的也是排过序的key-value串。然后我们可以有一个Metadata的表，里面存放的就是每个小表的存储位置。这样的话，一个很大的表就等于很多小表的集合，每一个小表又是排序过的K-V对的集合。

![image-20201206190011668](http://image.hihia.top/Screenshot/image-20201206190011668.png)

### 如何保存一个超大表

这里的思路类似，就是再拆一层，将大表拆成小表，将小表拆成小小表。

![image-20201206194226001](http://image.hihia.top/Screenshot/image-20201206194226001.png)

### 如何写数据

表有了，如何写数据呢？假设说我们需要添加一个K-V对如<b,yeah>，在整个系统架构中，我们会有内存和硬盘两部分，这里的硬盘也可以认为是底层的GFS（Google file system），在内存中我们可以存放小小表的位置。当我们写数据的时候，并不是直接就写入硬盘中，为了加快速度，我们会在内存中建一个表，将这个K-V对插入到这个表中。这里的好处就是如果我们是直接插入硬盘中的小小表，那么每一次插入的时候都需要对硬盘中的小小表进行排序，而内存中因为是随机存储所以排序是较容易的。所以此时一个tablet就等于内存表+小小表的集合。

![image-20201206195128285](http://image.hihia.top/Screenshot/image-20201206195128285.png)

### 内存表过大该如何？

这里会给内存表设置一个大小上限，一旦内存表满了，便将这个内存表写入硬盘，成为一个新的小小表。

![image-20201206195557727](http://image.hihia.top/Screenshot/image-20201206195557727.png)

### 如何避免内存表数据丢失

既然内存表中数据要满了才写入，那么没满的时候就丢失了怎么办？这里的办法就是有一个log（日志表），当我们要往内存表写入数据之前，先在硬盘上写入日志。写入日志的时候因为是顺序存储，所以性能还是比较快的。这样的话，一个tablet就等于内存表+小小表的集合+日志。

![image-20201206201913695](http://image.hihia.top/Screenshot/image-20201206201913695.png)

### 如何读数据

根据我们之前的架构，当我们要读数据的时候我们得再所有表中进行查找（包括内存表和硬盘中的表），因为在每个小小表内部是有序的，而在小小表之间是无序的。这就导致假如说我要查找b，那么我得在每张表中进行查找，性能无疑是很低的。

![image-20201206202225687](http://image.hihia.top/Screenshot/image-20201206202225687.png)

### 如何加速读数据

如何加速读数据？这里的思路便是去建立索引，每次将小小表写入硬盘时可以同时写一个索引在内存中，通过这个index来快速查找位置。所以这里的小小表就从键值对的集合变成了64KB大小的block的集合+索引。

![image-20201206202643218](http://image.hihia.top/Screenshot/image-20201206202643218.png)

### 如何继续加速读数据

之前的方法依旧进行了大量的硬盘遍历，这里的话我们可以使用布隆过滤器（bloom filter），它能以很少的花费以一个很大的正确率告诉我们查找的东西在不在一个集合里面。所以我们每次查找之前先用bloom filter先过滤一下就行了，只有当有可能在的时候才去遍历。

![image-20201206203927067](http://image.hihia.top/Screenshot/image-20201206203927067.png)

### 如何将表存入GFS

根据我们之前的概括，这里其实就是将小小表和日志放入到GFS的ChunkServer中，并且也会对应有副本。

![image-20201206204416942](http://image.hihia.top/Screenshot/image-20201206204416942.png)

### 架构

Client上会有对应的Client Library来提供对应的操作库函数等，同时也会有Chubby来提供表的metadata以及获取锁服务。之后，Client就能到特定的Tablet Server进行读写。Tablet Server之上也有一个Master负责处理metadata以及处理负载均衡，例如去协调放多少Tablet到某个Server上。更底层来说，GFS就提供了存放小小表和日志的能力，另外还有诸如Cluster Scheduling System来监控整个系统。

![image-20201206205359178](http://image.hihia.top/Screenshot/image-20201206205359178.png)

