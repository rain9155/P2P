## Pre

关于该应用的设计思路可以查看这篇文章，

由于一直想要做一个即时通讯系统，类似于微信的聊天系统，刚好要做课设，所以就仿微信的聊天界面，基于P2P的思想，做了一个简单的即时通讯系统，里面用到了java多线程并发编程(线程池、线程安全集合等)、TCP连接、Socket编程、UDP广播机制等知识点，实现了大文件的分段发送（分段接收），实现了简单的心跳机制、简单重连等操作。

> 心跳机制是什么？就每隔一段事件发一个探测，探测在线的用户是否存活。有些在线用户由于手机关机，不正常退出应用等会导致它无法退出登陆，这时就需要每隔一段时间探测它是否存活。

## Preview

{% asset_img p2p1.gif p2p1 %}

{% asset_img p2p2.gif p2p2 %}

{% asset_img p2p3.gif p2p3 %}

{% asset_img p2p4.gif p2p4 %}

## Screenshots

{% asset_img A1.png A1 %}

{% asset_img B1.png B1 %}

{% asset_img A2.png A2 %}

{% asset_img B2.png B2%}

{% asset_img A3.png A3 %}

{% asset_img B3.png B3 %}

