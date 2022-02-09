    THIS IS a MiraiPlugin FOR https://api.lolicon.app/#/setu
    这是一个能在 https://api.lolicon.app/#/setu 取图的Mirai插件
    
目前适配loliconapi v2 支持关键词和数量搜索

关键词搜索会先进行tag匹配，结果为空再进行keyword匹配

使用方式

    ·来张色图 //可被禁用
    ·来(1-5)张色图 //可被禁用
    ·来(1-5)张**色图 //可被禁用
    
    ·开启r18 //管理员以上和bot主人都有权限使用此命令
    ·关闭r18 //管理员以上和bot主人都有权限使用此命令
    ·开启色图 //管理员以上和bot主人都有权限使用此命令
    ·关闭色图 //管理员以上和bot主人都有权限使用此命令
    
config.yml中botOwnerId可以设置多个bot主人qq号

格式如下: 

    botOwnerId: 
     - 112233
     - 123456

本项目遵循AGPL v3开源协议
