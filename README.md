# Inspur Enterprise Cloud Client for Android

## 使用

### 准备

开发或调试本项目需要安装以下工具：

1. node.js（≥v6.9.0, ＜v7.0.0）
2. npm（≥3.10.0）
3. Android Studio（≥v2.2.0）
4. Python 2（≥v2.7.0）
5. JDK（≥v1.8）
6. Android SDK（v23)
7. Android Build Tools（v23.0.3)
8. ​

### 初始化

使用`git`获取源码后通过下面的命令进行初始化：

```shell
$ npm install
```

完成后会看到工程根目录出现node_modules目录。

### 调试

首先要启动Android模拟器环境，或者连接测试手机。请确保模拟器或测试机已经可以被开发调试使用。之后使用下面的命令启动React Native的Packager进行本地开发调试：

```shell
$ npm run packager
```

该命令会启动Packager Server，并通过`adb`工具将开发机本地8081端口映射到当前调试设备。

### 发布

应用最终发布前，需要使用下面的命令将React Native的源码文件打包为Bundle：

```shell
$ npm run bundle
```

执行成功后会在`$project_root/app/src/main/assets`生成Bundle文件。之后按照正常的Android工程发布方式发布即可。