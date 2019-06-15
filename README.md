# TickerManager_Backend

课程设计：电影管理系统后端

[API文档传送门](https://apizza.net/pro/#/project/4843d02763e705a759ba3720197c216b/browse)

## 运行
**Step 1** 使用mysql创建数据库`ticket_manager`并运行

**Step 2** 修改对应平台的文件夹下的`application.properties`数据库配置

- Windows: src/main/resources/application.properties
- MacOS: src/main/resources_mac/application.properties
- Linux(Production env): src/main/resource_linux/application.properties

```
# 修改为你的数据库用户名
spring.datasource.username=xxxx
# 修改为你的数据库密码
spring.datasource.password=xxxx
```

**Step 3** 运行redis-server

**Step 4** 运行服务器
- 使用idea导入运行
- 使用gradle运行
```
./gradlew bootRun
```
- 使用脚本运行
 
脚本目录：`script/start_run.py`。

修改对应平台数据库密码：

``` python
// MacOS
pass_mac = 'xxxx'
// Linux
pass_prod = 'xxxx'
// Windows
pass_win = 'xxxx'
```

用户名设置为了`root`，如有需要更改，在以下位置修改：
``` python
def drop_old_data(password):
    // change `root` to your database username
    db = pymysql.connect("localhost", "root", password,
                         charset='utf8mb4',

                         cursorclass=pymysql.cursors.DictCursor)
    ...
```

运行（需要pymysql依赖）：
```
# 命令行参数参考源码
python script/start_run.py
```

``
``

## 其他
**电影图片爬虫脚本**

若要服务器中存有图片url，需要在使用脚本爬取图片。脚本位置：
`script/image_crawller.py`

直接运行即可：
```
python script/image_crawller.py
```

运行后将生成的文件夹下的`urls.txt`放在项目根目录，并作为springBoot应用运行时的命令行参数传入：
```
./gradlew bootRun urls.txt
```