# P2P
### 一个基于P2P的局域网即时通信应用
## Pre

关于该应用的设计思路可以查看这篇文章[基于P2P的局域网即时通信应用](https://juejin.im/post/5d2aa766f265da1bc752687b)，

由于一直想要做一个即时通讯系统，类似于微信的聊天系统，刚好要做课设，所以就仿微信的聊天界面，基于P2P的思想，做了一个简单的即时通讯系统，里面用到了java多线程并发编程(线程池、线程安全集合等)、TCP连接、Socket编程、UDP广播机制等知识点，实现了大文件的分段发送（分段接收），实现了简单的心跳机制、简单重连等操作。

该应用的聊天界面的多布局实现是使用了自己封装的[BaseAdapter](https://github.com/rain9155/BaseAdapter),发送消息的聊天气泡是使用[SVG](https://blog.csdn.net/Rain_9155/article/details/89262144)画的，这样就可以满足图片的伸缩性。

## Preview

![p2p1](screenshots/p2p1.gif)

![p2p2](screenshots/p2p2.gif)

![p2p3](screenshots/p2p3.gif)

![p2p3](screenshots/p2p3.gif)

## Screenshots

![](screenshots/p2p1.png)

![](screenshots/p2p2.png)

![](screenshots/p2p3.png)

## TODO

- **用户头像用TCP发送后断开连接的合适时机**

因为发送头像时就已经建立了TCP连接，如果某一方发送完后就断开，会导致另外一方无法发送，所以目前这个问题还没有解决，即TCP连接发送完头像后没有断开，不过考虑到用户有可能会聊天，断不断开也没有关系。

- **聊天记录的保存**

当你这个聊天退出返回后，再次进来，上次的聊天记录没有保存。

- **新信息的提醒**

类似与微信一样，有新消息时列表那里会有红点提示，这个程序中没有这个功能，用户不知道消息的到来。

- **断线重连**

在聊天的过程中由于一些其他的原因导致一方连接断开，无法发送消息，这时要采取一些重连措施。

## Library
文件选择 - [MaterialFilePicker](https://github.com/nbsp-team/MaterialFilePicker) <br>
图片裁剪 - [Android-Image-Cropper](https://github.com/ArthurHub/Android-Image-Cropper)

## Licensed
```
Copyright 2019 rain9155

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License a

          http://www.apache.org/licenses/LICENSE-2.0 
          
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
