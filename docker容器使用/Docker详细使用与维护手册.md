## Docker简介

* Docker是一个开源的容器化平台，允许将应用及其依赖打包成轻量、可移植的容器。
* 核心组件:
  * 镜像（image）:只读模板，用于创建容器。
  * 容器（container）：镜像的运行实例
  * Docker Daemon ：后台服务，管理镜像，容器，网络，存储
  * Docker Client：命令工具，与Daemon通信

## Linux安装Docker

* CentOS/RHEL/Rocky Linux

  ```bash
  sudo yum install -y yum-utils
  sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
  sudo yum install -y docker-ce docker-ce-cli containerd.io
  sudo systemctl start docker
  sudo systemctl enable docker
  sudo usermod -aG docker  $ USER  # 免 sudo 使用 docker
  ```
* Ubuntun / Debian

  ```bash
  sudo apt update
  sudo apt install -y ca-certificates curl gnupg lsb-release
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
  echo "deb [arch= $ (dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu  $ (lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
  sudo apt update
  sudo apt install -y docker-ce docker-ce-cli containerd.io
  sudo systemctl start docker
  sudo usermod -aG docker  $ USER
  ```

## 镜像管理

* 命令:

| 命令                                                  | 说明                   |
| ----------------------------------------------------- | ---------------------- |
| `docker images`                                     | 列出本地镜像           |
| `docker pull nginx:alpine`                          | 拉取镜像               |
| `docker build -t myapp:v1 .`                        | 从 Dockerfile 构建镜像 |
| `docker rmi <IMAGE_ID>`                             | 删除镜像               |
| `docker tag myapp:v1 registry.example.com/myapp:v1` | 打标签（用于推送）     |
| `docker push registry.example.com/myapp:v1`         | 推送镜像到仓库         |

## 容器管理

* 启动容器

  ```bash
  # 后台运行（-d），映射端口（-p），挂载目录（-v），设置环境变量（-e）
  docker run -d \
    --name webapp \
    -p 8080:80 \
    -v /host/data:/app/data \
    -e ENV=prod \
    nginx:alpine
  ```
* 常见操作命令

  | `docker ps`                | **查看运行中的容器**         |
  | ---------------------------- | ---------------------------------- |
  | `docker ps -a`             | **查看所有容器（含已停止）** |
  | `docker stop <name/id>`    | **停止容器**                 |
  | `docker start <name/id>`   | **启动已停止容器**           |
  | `docker restart <name/id>` | **重启容器**                 |
  | `docker rm <name/id>`      | **删除容器（需先停止）**     |
  | `docker rm -f <name/id>`   | **强制删除运行中容器**       |
  | `docker logs -f <name>`    | **实时查看日志**             |

  docker exec -it `<name>` /bin/sh	进入容器终端
* 自动清理

  ```bash
  # 删除所有已停止的容器
  docker container prune

  # 删除无用镜像、网络、构建缓存
  docker system prune -a
  ```
* 网络模式
  有三种 bridge桥接（默认）：容器通过虚拟网桥通信，可端口映射
  host：共享宿主机网络（高性能，但有端口冲突的风险）
  none：无网络
* 自定义网络

  ```bash
  # 创建自定义 bridge 网络
  docker network create mynet

  # 启动容器加入网络
  docker run -d --name app --network mynet myapp
  docker run -d --name db  --network mynet postgres

  # 容器间可通过名称直接通信（如 ping db）
  ```

## 数据持久化（Volume）

* Bind Mounts :挂载宿主机目录**（**`-v /host/path:/container/path`）
* Volumes : Docker管理的存储(推荐)

  ```bash
  docker volume create mydata
  docker run -v mydata:/app/data myapp
  ```
* 管理Volume

  ```bash
  docker volume ls
  docker volume inspect mydata
  docker volume rm mydata
  docker volume prune  # 清理未使用的 volume
  ```

## 查看容器详细信息

* 语句

  ```bash
  # 查看单个容器全部配置（JSON）
  docker inspect <container>

  # 批量查看所有容器 IP
  docker inspect --format='{{.Name}} {{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}'  $ (docker ps -q)

  # 查看端口映射
  docker port <container>
  ```

## 日志与监控

* 语句

  ```bash
  docker logs <container>          # 查看日志
  docker logs --tail 100 <container>  # 最后 100 行
  docker logs -f <container>       # 实时跟踪

  docker stats  			#实时的资源消耗
  ```

## 常见问题排查

* 说明

| 问题               | 排查命令                                                              |
| ------------------ | --------------------------------------------------------------------- |
| 容器启动失败       | `docker logs <container>`                                           |
| 网络不通           | `docker inspect <container>`查 IP；`docker network inspect <net>` |
| 磁盘空间不足       | `docker system df`；清理：`docker system prune -a`                |
| 端口被占用         | `ss -tulnp \| grep 8080`或 `lsof -i :8080`                         |
| 权限拒绝（挂载卷） | 检查 SELinux：加 `:Z`或 `:z`标签，如 `-v /host:/cont:Z`         |

## 常用速查表

* 说明

| 场景                           | 命令                                                                       |
| ------------------------------ | -------------------------------------------------------------------------- |
| 进入运行中容器                 | `docker exec -it <name> sh`                                              |
| 复制文件进容器                 | `docker cp file.txt <container>:/path/`                                  |
| 查看 Docker 存储位置           | `/var/lib/docker/`                                                       |
| 修改 Docker 配置（如镜像加速） | 编辑 `/etc/docker/daemon.json`                                           |
| 镜像加速（国内）               | 添加 `"registry-mirrors": ["https://<your-mirror>.mirror.aliyuncs.com"]` |

@

@

@

@

@

@

@

@
