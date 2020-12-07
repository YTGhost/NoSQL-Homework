# MapReduce读后感

[TOC]

## MapReduce的层次

MapReduce是一个编程模型，也是一个处理和生成超大数据集的算法模型的相关实现。为了处理海量的原始数据，比如文档抓取、Web请求日志、倒排索引等数据处理运算，虽然它们在概念上很容易理解，然而由于输入的数据量巨大，因此要想在可接受的时间内完成运算，只有将这些计算分布在成百上千的主机上，而如何处理并行计算、如何分发数据、如何处理错误，这些就是MapReduce要干的事情。而在层次上，MapReduce也应用在了Bigtable和GFS上。

![image-20201206205819592](http://image.hihia.top/Screenshot/image-20201206205819592.png)

## 什么是Map和Reduce

### 什么是Map

Map的本质就是拆解，举个例子，有一辆红色的汽车和一群工人，工人们把汽车拆解成零件，这就是Map。

![image-20201206212708660](http://image.hihia.top/Screenshot/image-20201206212708660.png)

### 什么是Reduce

Reduce的本质就是组合，还是举汽车的例子，把拆解出来的汽车零件，以及从其他机械中拆解出来的零件进行组合，就能得到变形金刚（开个玩笑）。

![image-20201206212903595](http://image.hihia.top/Screenshot/image-20201206212903595.png)

### MapReduce六大过程

MapReduce的六大过程：

- input
- Split
- Map
- Shuffle
- Reduce
- Finalize

还是举一下例子，比如说我们现在有一个汉堡店，我们一开始有很多**Input**，即水果、面包、蔬菜等，我们也有很多的厨子，不同的厨子分到了不同的水果面包，这个过程就是**Split**。厨子拿到手上后便开始将这些东西切碎处理，比如说把面包切成一片一片的，这个过程就是**Map**。将处理过的材料放入到诸如烤箱里，冷藏柜里的过程就是**Shuffle**。而在顾客来的时候，这些材料就会根据顾客的需要来进行组合成顾客想要的东西，这个过程就是**Reduce**。组合成顾客想要的东西后便会等待顾客进行付费，这个过程就是**Finalize**。

![image-20201206214228269](http://image.hihia.top/Screenshot/image-20201206214228269.png)

## 具体如何处理

比如说我们现在有1TB（10TB甚至1PB）的数据，那么我们现在可能会有两个问题：

- 如何统计单词数
- 如何建立倒排索引

### 如何统计单词出现数

比如说现在有文档需要统计，文档的每一行是不同的单词组合起来的，这个文档就是**Input**。这里就可以有多个worker来去把文档切分成一行一行并拿到每个work手上，这个过程就是**Split**。将每个work手上的一行文档拆解成单词和对应的单词数，这个过程就是**Map**。再将相同的单词放入不同的“盒子”里，这个过程就是**Shuffle**。而**Reduce**就是把前一步放在一个“盒子”里的单词做一个总和。最后的**Finalize**就是将这些总和放到一块。

![image-20201206221146086](http://image.hihia.top/Screenshot/image-20201206221146086.png)

### 如何建立倒排索引

比如说现在有一些文档，我们的**Input**是文档的编号以及对应文档中的一行。Split的过程便是多个woker去拿**Input**。**Map**的过程是将woker分到的句子中单词拆解出来，拆解成<单词，对应的文档编号>这样的格式。

**Shuffle**这里假设说要把单词分成两个部分，一个部分是首字母为j以前的，一个部分是首字母为j和j以后的，那这里的话就可以有两个worker去**Map**处理好的结果中去拿数据，同时也可以排个序。

**Reduce**的话就可以把相同的归并到一起，比如说food出现在了两个文档里，就可以把它们归并一下。

最后**Finalize**合在一起就是了。

![image-20201206222202566](http://image.hihia.top/Screenshot/image-20201206222202566.png)

## MapReduce的架构

MapReduce会先考虑将数据拆成几份，然后再去分配对应的worker。这里会有一个Master worker，它会作为用户的代理来协调其他worker。读数据的worker会在本地把数据Map完后，写到本地硬盘上。后面的worker就能远程地Shuffle并在本地Reduce，最后将结果写到final file里就是Finalize。

![image-20201206222508017](http://image.hihia.top/Screenshot/image-20201206222508017.png)