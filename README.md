# Thanks [LoliconApi](api.lolicon.app)
关键词匹配搜索支持tag和keyword搜索
# 本插件支持[MiraiConsole权限系统](https://docs.mirai.mamoe.net/console/Permissions.html#%E5%88%A4%E6%96%AD%E6%9D%83%E9%99%90)配置
即本插件需要使用Console的权限系统进行权限配置才能使用
# 默认command
    (/)random
    (/)keyword <keyword>
    (/)manager 
        setuon
        setuoff
        r18on
        r18off
# ReplyConfig.yml //自定义bot的回复 留空可禁用
    //匹配到命令开始搜索时bot的回复
    startSearchingReply: ''
    //匹配到命令但禁止色色时bot的回复
    refuseReply: 不可以色色！
    //启用或关闭色图功能的回复
    setuOnReply: 已开启色图
    setuOffReply: 已关闭色图
    //启用或关闭r18功能的回复
    r18OnReply: 已开启r18
    r18OffReply: 已关闭r18
    //没有搜索到结果时的回复
    noMatchResultReply: 你的xp好怪。。。
    //无法连接上LoliconApi时的回复
    connectionFailureReply: ''
# NetworkConfig.yml //网络参数设置 代理使用http代理
    connectTimeout: 60 //以下为超时设置
    callTimeout: 60 
    readTimeout: 60
    writeTimeout: 60
    proxyLink: i.pixiv.re //反代网址 可连接外网的情况下建议i.pximg.net
    proxySwitch: true //代理开关
    proxyAddress: 127.0.0.1 //代理地址
    proxyPort: 10809 //代理端口
# CommandConfig.yml //自定义你的命令
    random: 来点色图  //无关键词随机获取色图命令
    keyword: 来点 //有关键词获取色图命令
    manager: 拉bot //管理命令
# RecallTimeConfig.yml //设置撤回时间
    //撤回功能填入1s-120s时会生效，其他数字则禁用
    recallTime: -1
