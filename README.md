# alltv

获取 斗鱼、虎牙、bilibili直播源、弹幕信息

#### 预览地址：[在线api文档](http://debugers.com:8888/swagger-ui.html)

**弹幕预览请移步下面：**

整合了 swagger2 本地运行访问 http://localhost:8888/swagger-ui.html 即可查看api文档


#### New Feature

* 新增触手直播
* 新增获取bilibili直播间弹幕(websocket 具体食用方法在下面)

#### bilibili弹幕食用方法

首先先讲一下机制，采用的是Java Socket ,

首先需要房间真实的`room_id`怎么获取api里面有，然后用WebSocket连接bilibili的弹幕服务器，连接成功后需要认证，不然服务器会断开，然后每30s需要发送一次心跳包，告诉服务器你任然存在，具体时间也可以不是30s，反正最多50s吧，没有具体测试过。

然后客户端向服务器发送的数据，都需要经过封包处理

**封包由头部和数据组成，字节序均为大端模式**

**头部格式：**

| 偏移量 | 长度 | 含义                  |
| ------ | ---- | --------------------- |
| 0      | 4    | 封包总大小            |
| 4      | 2    | 头部长度              |
| 6      | 2    | 协议版本，目前是1     |
| 8      | 4    | 操作码（封包类型）    |
| 12     | 4    | sequence，可以取常数1 |

已知的操作码：

| 操作码 | 含义                              |
| ------ | --------------------------------- |
| 2      | 客户端发送的心跳包                |
| 3      | 人气值，数据不是JSON，是4字节整数 |
| 5      | 命令，数据中`['cmd']`表示具体命令 |
| 7      | 认证并加入房间                    |
| 8      | 服务器发送的心跳包                |

**数据格式：**一般为JSON字符串UTF-8编码

加入这里我们需要认证，则需要发送(以下仅是数据，不包括头部的二进制包,需另作处理)：

```json
{
  "uid": 但是最好随机生成,
  "roomid": 房间ID,
  "protover": 1,
  "platform": "web",
  "clientver": "1.10.6"
}
```

**然后就是发送心跳包最好30s发送一次**

接受bilibili服务端返回的数据，返回的格式根据内容的不同而改变，但有一项是不会变的`cmd`	代表操作命令

已知的命令：

| 命令                          | 含义             |
| ----------------------------- | ---------------- |
| DANMU_MSG                     | 收到弹幕         |
| SEND_GIFT                     | 有人送礼         |
| WELCOME                       | 欢迎加入房间     |
| WELCOME_GUARD                 | 欢迎房管加入房间 |
| SYS_MSG                       | 系统消息         |
| ROOM_REAL_TIME_MESSAGE_UPDATE | 粉丝数更新       |

其实还有一些不常用就不具体说了，项目里枚举类型里面写着的有。



**重点来了，这里我说本地的一个websocket,将收到的消息进行转发，来达到一个线上预览的效果，如果你有其他用途请自行更改**。

`websocket` 请求地址: `ws://localhost:8888/bilibili/房间号`

连接成功后就会收到转发回来的直播间信息，目前只做了弹幕、粉丝更新、用户进入、和房间人数的一个`handle`

##### 线上预览 ：`ws://debugers.com:8888/bilibili/房间号`

这里顺便发一个线上`websocket`的一个测试工具：[websocket在线测试工具](http://coolaf.com/tool/chattest)

![testwebsocket](https://harryzhang-blog.oss-cn-shanghai.aliyuncs.com/testwebsocket_1587707812677.png)


#### docker (已更新)

```bash
docker pull harryzhang6/alltv

docker run --rm -it -d --name alltv -p 8888:8888  harryzhang6/alltv
​```

#### 后续开发:

- [x] 修复 BILIBILI 直播源获取

- [x] 提供线上api预览

- [x]  加入其他平台，如触手、企鹅电竞

- [ ]  可视化web界面

- [ ] 增加其他平台获取弹幕(websocket)

```