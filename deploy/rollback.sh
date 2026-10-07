#!/usr/bin/env bash
# 用法：服务器上执行 bash /opt/myblog/rollback.sh
set -e
cd /opt/myblog

[ -f backend/app.jar.prev ] || { echo "❌ 没有可回滚的后端版本"; exit 1; }

cp backend/app.jar.prev backend/app.jar
if [ -d frontend/dist.prev ]; then
  rm -rf frontend/dist
  mv frontend/dist.prev frontend/dist
fi

docker compose up -d backend nginx
docker compose restart nginx

curl -fsS http://127.0.0.1:8081/api/config/all >/dev/null && echo "✅ 回滚完成"
