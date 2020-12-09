# redis作业说明文档

大体的效果跟之前课上演示的差不多，一共封装了四种类型：num、Freq、list和str，基本的常用操作都有。

## json文件定义和读取

json文件在src/main/resources目录下，分为actions.json和counter.json。文件监听器以轮询的形式进行文件监听，有对应的锁进行保护，能自适应json文件变化。

## freq周期统计

周期统计可以以小时为颗粒进行统计。

<img src="http://image.hihia.top/Screenshot/image-20201209235215158.png" alt="image-20201209235215158" style="zoom:50%;" />

## 工厂模式

使用了工厂模式进行不同类型counter的操作：

<img src="http://image.hihia.top/Screenshot/image-20201209235355681.png" alt="image-20201209235355681" style="zoom: 33%;" />

## 程序入口

程序入口为：src/main/java/com/bjtu/redis/目录下的Main，运行后有对应的client调用：

<img src="http://image.hihia.top/Screenshot/image-20201209235611724.png" alt="image-20201209235611724" style="zoom: 50%;" />

