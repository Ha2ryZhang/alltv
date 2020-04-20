# alltv
获取 斗鱼、虎牙、bilibili直播源

#### 预览地址：[在线api文档](http://debugers.com:8888//swagger-ui.html)

整合了 swagger2 本地运行访问 http://localhost:8080/swagger-ui.html 即可查看api文档


#### docker 
```bash
docker pull harryzhang6/alltv

docker run --rm -it -d --name alltv -p 8888:8888  harryzhang6/alltv
```

#### 后续开发:

- [x] 修复 BILIBILI 直播源获取

- [x] 提供线上api预览

- [ ]  加入其他平台，如触手、企鹅电竞

- [ ]  可视化web界面

- [ ] 增加获取弹幕(websocket)
