    THIS IS a MiraiPlugin FOR https://api.lolicon.app/#/setu
    这是一个能在 https://api.lolicon.app/#/setu 取图的Mirai插件
    
目前适配loliconapi v2 支持关键词和数量搜索

关键词搜索会先进行tag匹配，结果为空再进行keyword匹配

使用方式在config.yml中

config.yml
    //注意使用正则表达式
    command: '来点(.*)色图'
    //反代地址，非代理地址，非必要不要修改
    proxyAddress: i.pixiv.re
    setuOnCommand: 开启色图
    setuOffCommand: 关闭色图
    r18OnCommand: 开启r18
    r18OffCommand: 关闭r18
    startSearchingReply: ''
    refuseReply: 不可以色色！
    setuOnReply: 已开启色图
    setuOffReply: 已关闭色图
    r18OnReply: 已开启r18
    r18OffReply: 已关闭r18
    noMatchResultReply: 你的xp好怪。。。
    connectionFailureReply: ''
    recallTime: -1
    botOwnerId: 
      - 947560351

本项目遵循AGPL v3开源协议
