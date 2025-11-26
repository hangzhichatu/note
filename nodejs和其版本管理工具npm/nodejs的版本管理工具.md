## NVM node.js的版本管理工具
* 安装步骤：
卸载你电脑上已有的 Node.js（必须！）
下载安装 nvm-setup.zip
以管理员身份运行安装程序
安装完成后，打开 新的 CMD 或 PowerShell

* 常用指令
 ```Powershell
    nvm install 18.18.0
    nvm use 18.18.0
    nvm list
 ```

* 实际使用
```Bash
    # 当前用 16.x（老项目）
    $ nvm use 16.20.2
    Now using node v16.20.2

    $ node -v
    v16.20.2

    # 切到 20.x（新项目）
    $ nvm use 20.12.0
    Now using node v20.12.0

    $ node -v
    v20.12.0
```

* ps 使用nvm进行版本管理的话是需要删除系统中已有的node.js的，否则会报错