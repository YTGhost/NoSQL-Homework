# neo4j

沙盒地址：https://10-0-1-128-33603.neo4jsandbox.com/browser/

因为沙盒有效期只有三天，所以大概十二号晚上以后就看不了了，所以这里截图替代。

## 导入数据

```
CREATE
	(nAlice:User {name: 'Alice', seed: 42}),
	(nBridget: User {name: 'Bridget', seed: 42}),
	(nCharles: User {name: 'Charles', seed: 42}),
	(nDoug: User {name: 'Doug'}),
	(nMark: User {name: 'Mark'}),
	(nMichael: User {name: 'Michael'}),
	(nAlice)-[:LINK {weight: 1}]->(nBridget),
	(nAlice)-[:LINK {weight: 1}]->(nCharles),
	(nCharles)-[:LINK {weight: 1}]->(nBridget),
	(nAlice)-[:LINK {weight: 5}]->(nDoug),
	(nMark)-[:LINK {weight: 1}]->(nDoug),
	(nMark)-[:LINK {weight: 1}]->(nMichael),
	(nMichael)-[:LINK {weight: 1}]->(nMark);
```

![image-20201209222934803](http://image.hihia.top/Screenshot/image-20201209222934803.png)

## 验证导入数据

```
match (u:User) return *
```

![image-20201209223002185](http://image.hihia.top/Screenshot/image-20201209223002185.png)

## 准备工作

```
CALL gds.graph.create(
    'myGraph',
    'User',
    {
        LINK: {
            orientation: 'UNDIRECTED'
        }
    },
    {
        nodeProperties: 'seed',
        relationshipProperties: 'weight'
    }
)
```

![image-20201209223030451](http://image.hihia.top/Screenshot/image-20201209223030451.png)

## 评估算法所需资源

```
CALL gds.louvain.write.estimate('myGraph', { writeProperty: 'community' })
YIELD nodeCount, relationshipCount, bytesMin, bytesMax, requiredMemory
```

![image-20201209223054038](http://image.hihia.top/Screenshot/image-20201209223054038.png)

## 运行louvain算法

### 返回流结果

```
CALL gds.louvain.stream('myGraph')
YIELD nodeId, communityId, intermediateCommunityIds
RETURN gds.util.asNode(nodeId).name AS name, communityId, intermediateCommunityIds
ORDER BY name ASC
```

![image-20201209223146869](http://image.hihia.top/Screenshot/image-20201209223146869.png)

### 返回社区数

```
CALL gds.louvain.stats('myGraph')
YIELD communityCount
```

![image-20201209223211784](http://image.hihia.top/Screenshot/image-20201209223211784.png)

### 返回模块度

```
CALL gds.louvain.mutate('myGraph', { mutateProperty: 'communityId' })
YIELD communityCount, modularity, modularities
```

![image-20201209223231763](http://image.hihia.top/Screenshot/image-20201209223231763.png)

