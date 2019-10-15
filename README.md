# Serverless 微服务实践-移动应用包分发服务

## 背景

阿里云函数计算是事件驱动的全托管计算服务。通过函数计算，您无需管理服务器等基础设施，只需编写代码并上传。函数计算会为您准备好计算资源，以弹性、可靠的方式运行您的代码，并提供日志查询、性能监控、报警等功能。借助于函数计算，您可以快速构建任何类型的应用和服务，无需管理和运维。而且，您只需要为代码实际运行所消耗的资源付费，代码未运行则不产生费用。

移动应用的打包和分发呈现明显的峰谷效用，用户常常需要短时间内准备大量资源保障分发的实时性，完成分发后又需要及时释放资源，降低成本。这里我们提供一个 [fun](https://github.com/alibaba/funcraft) 模板，帮助我们更快地搭建一个基于[函数计算](https://helpcdn.aliyun.com/product/50980.html)构建 Serverless 架构的包分发服务，在开发运维效率，性能和成本间取得良好的平衡。

在分包过程中，下载/修改/上传是一个比较消耗资源的任务，需要消耗大量的计算/网络资源。并且分包任务只在应用发布新版本时才会发生，需要在尽可能短的时间内完成。针对这种有明显波峰波谷的场景，非常适合使用函数计算来完成。更重要的是这个服务是具有弹性伸缩和高可用能力的。

<a name="1"></a>
## APK分包简介

![apk分包简介.jpg](/figures/apk分包简介.jpg)

更多参考 [**[函数计算] Serverless 微服务实践-移动应用包分发服务**](https://yq.aliyun.com/articles/699972)


<a name="Ss7xm"></a>
## 准备工作：

<a name="gjNdw"></a>
### 1.安装 node

```bash
curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.33.5/install.sh | bash
nvm install 8
```

<a name="rXtee"></a>
### 2.安装 fun 工具

```bash
npm install @alicloud/fun -g
```

fun 工具的某些子命令可能会用到 docker，所以你需要安装好 docker，具体参考文档：[Fun 安装教程](https://github.com/aliyun/fun/blob/master/docs/usage/installation-zh.md)。

<a name="IVZmu"></a>
### 3.apk 包准备

在这个实验中，我们会使用一个示例的 apk 包，可以从这里下载 [qq-v2.apk](https://yq.aliyun.com/go/articleRenderRedirect?url=http%3A%2F%2Ffc-imm-demo-cici.oss-cn-hangzhou.aliyuncs.com%2Fapk%2Fqq-v2.apk)

写入渠道信息的方式，我们使用美团的开源工具 [walle](https://yq.aliyun.com/go/articleRenderRedirect?spm=a2c4e.11153940.0.0.54774df4JFNSwx&url=https%3A%2F%2Fgithub.com%2FMeituan-Dianping%2Fwalle)，下载 [walle-cli-all.jar](https://yq.aliyun.com/go/articleRenderRedirect?url=http%3A%2F%2Ffc-imm-demo-cici.oss-cn-hangzhou.aliyuncs.com%2Fapk%2Fwalle-cli-all.jar) 包备用。

下载 [qq-v2.apk](https://yq.aliyun.com/go/articleRenderRedirect?url=http%3A%2F%2Ffc-imm-demo-cici.oss-cn-hangzhou.aliyuncs.com%2Fapk%2Fqq-v2.apk) ，上传到自己的 oss bucket中：

![image.png](/figures/apk包分发前.png)

<a name="TLgrD"></a>
## 快速开始:

<a name="a4979714"></a>
### 1.通过 fun 模板生成项目骨架

使用 fun init 命令可以快捷的将本模板项目初始化到本地，执行命令 ：

```powershell
$ fun init -n xxx https://github.com/coco-super/package-distribution-service-for-serverless
```

其中 -n 表示要作为文件夹生成的项目名称。默认值是 fun-app。更多fun init 命令格式选项说明请参考云栖文章[开发函数计算的正确姿势 —— 使用 Fun Init 初始化项目](https://yq.aliyun.com/articles/674363)。

```powershell
$ fun init -n apk https://github.com/coco-super/package-distribution-service-for-serverless
start cloning...
Cloning into '.fun-init-cache-2fc2d680-eeff-11e9-a930-6fd4d1ac6506'...
remote: Enumerating objects: 23, done.
remote: Counting objects: 100% (23/23), done.
remote: Compressing objects: 100% (16/16), done.
remote: Total 23 (delta 0), reused 18 (delta 0), pack-reused 0
Unpacking objects: 100% (23/23), done.
finish clone.
? Please input a oss bucket name? sunfeiyu
Start rendering template...
+ /Users/ellison/simple-fc-uncompress-service-for-oss/apk
+ /Users/ellison/simple-fc-uncompress-service-for-oss/apk/.funignore
+ /Users/ellison/simple-fc-uncompress-service-for-oss/apk/pom.xml
+ /Users/ellison/simple-fc-uncompress-service-for-oss/apk/src
+ /Users/ellison/simple-fc-uncompress-service-for-oss/apk/src/main
+ /Users/ellison/simple-fc-uncompress-service-for-oss/apk/src/main/java
+ /Users/ellison/simple-fc-uncompress-service-for-oss/apk/src/main/java/example
+ /Users/ellison/simple-fc-uncompress-service-for-oss/apk/src/main/java/example/App.java
+ /Users/ellison/simple-fc-uncompress-service-for-oss/apk/target
+ /Users/ellison/simple-fc-uncompress-service-for-oss/apk/target/classes
+ /Users/ellison/simple-fc-uncompress-service-for-oss/apk/target/classes/example
+ /Users/ellison/simple-fc-uncompress-service-for-oss/apk/target/classes/example/App.class
+ /Users/ellison/simple-fc-uncompress-service-for-oss/apk/template.yml
finish rendering template.
```

上面会提示：

输入一个 oss 的 bucket，注意 oss Bucket 是全球唯一的，上面的 chrome-headless 已经被占用了，请换一个新的名称或者一个已经创建好的（已经创建好的，请确保 region 一致）。

和 **apk 包准备**中提到的 oss bucket name 是同一个，本示例为：`sunfeiyu`

<a name="KSZvl"></a>
### 4. 编译
在模版项目的根目录 **apk** 下执行 `fun build` 命令编译：

```powershell
$ fun build 
using template: template.yml
start building function dependencies without docker

building apk/apk
running task flow MavenTaskFlow
running task: MavenCompileTask
running task: MavenCopyDependencies
running task: CopyMavenArtifacts

Build Success

Built artifacts: .fun/build/artifacts
Built template: .fun/build/artifacts/template.yml

Tips for next step
======================
* Invoke Event Function: fun local invoke
* Invoke Http Function: fun local start
* Deploy Resources: fun deploy
```

查看编译后的交付产物：

```powershell
$ cd .fun/build/artifacts/apk/apk/lib 
$ ls
aliyun-java-sdk-core-3.4.0.jar aliyun-java-sdk-sts-3.0.0.jar  commons-logging-1.2.jar        httpcore-4.4.1.jar             jettison-1.1.jar
aliyun-java-sdk-ecs-4.2.0.jar  aliyun-sdk-oss-3.6.0.jar       fc-java-core-1.3.0.jar         javax.servlet-api-3.1.0.jar    stax-api-1.0.1.jar
aliyun-java-sdk-ram-3.0.0.jar  commons-codec-1.9.jar          httpclient-4.4.1.jar           jdom-1.1.jar
```

<a name="ms5sl"></a>
#### 添加 jar 包
将准备工作中下载的 下载 `walle-cli-all.jar` ，放到 `.fun/build/artifacts/apk/apk/lib` 目录下


<a name="290f0a78"></a>
### 5.服务部署
在模版项目的的根目录 **apk** 下执行 `fun deploy` 部署到云端。

```powershell
$ fun deploy
using template: .fun/build/artifacts/template.yml
using region: cn-shanghai
using accountId: ***********8320
using accessKeyId: ***********mTN4
using timeout: 60

Waiting for service apk to be deployed...
        make sure role 'aliyunfcgeneratedrole-cn-shanghai-apk' is exist
        role 'aliyunfcgeneratedrole-cn-shanghai-apk' is already exist
        attaching policies AliyunOSSFullAccess to role: aliyunfcgeneratedrole-cn-shanghai-apk
        attached policies AliyunOSSFullAccess to role: aliyunfcgeneratedrole-cn-shanghai-apk
        Waiting for function apk to be deployed...
                Waiting for packaging function apk code...
                The function apk has been packaged. A total of 15 files files were compressed and the final size was 3.13 MB
        function apk deploy success
service apk deploy success
```

<a name="3CaB7"></a>
### 6.执行函数

提供两种方式：

1. 登陆阿里云函数计算[控制台](https://fc.console.aliyun.com)，手动执行。

![image.png](/figures/控制台执行.png)

2. `fun invoke apk`  命令远端调用：

```powershell
$ fun invoke apk
using template: template.yml
========= FC invoke Logs begin =========
FC Invoke Start RequestId: 23f34cc4-0cd3-40e0-9a04-f6586cf29be6
2019-10-14 17:06:35.971 [INFO] [23f34cc4-0cd3-40e0-9a04-f6586cf29be6] cmd: java -jar /code/walle-cli-all.jar put -c aliyun-fc /tmp/input.apk /tmp/output.apk
2019-10-14 17:06:36.152 [INFO] [23f34cc4-0cd3-40e0-9a04-f6586cf29be6] Success!

FC Invoke End RequestId: 23f34cc4-0cd3-40e0-9a04-f6586cf29be6

Duration: 666.13 ms, Billed Duration: 700 ms, Memory Size: 1024 MB, Max Memory Used: 254.55 MB
========= FC invoke Logs end =========

FC Invoke Result:
Success
```


<a name="db06c78d"></a>
## 查看结果

登陆 oss 查看已经生成新的 apk 包：

![image.png](/figures/新的apk包.png)

查看渠道信息是否写入：

将 oss 中 qq-v2-signed.apk 下载到本地。

```powershell
~/Downloads                                                                                                                                             ⍉
▶ java -jar /Users/ellison/Downloads/walle-cli-all.jar show qq-v2-signed.apk
/Users/ellison/Downloads/qq-v2-signed.apk : {channel=aliyun-fc}
```

渠道信息 `{channel=aliyun-fc}` 已写入，成功！

<a name="2473ec5a"></a>
## 参考阅读

1. [Fun (Fun with Serverless) 工具](https://github.com/aliyun/fun/)
1. [Fun Init 自定义模板](https://yq.aliyun.com/articles/674364)
