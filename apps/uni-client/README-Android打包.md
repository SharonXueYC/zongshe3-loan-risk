# 安卓 APP 打包指南（uni-app）

## 功能页面

| 页面 | 路径 | 功能 |
|------|------|------|
| 登录 | pages/login/login | 账号登录、配置服务器 |
| 首页 | pages/home/home | 额度、快捷入口 |
| 申请贷款 | pages/loan/apply | 提交申请、触发评分卡风控 |
| 审核进度 | pages/loan/progress | 我的申请、风控详情 |
| 还款 | pages/repay/repay | 还款计划、在线还款 |

## 开发调试

```bash
cd apps/uni-client
npm install

# H5 预览
npm run dev:h5

# 安卓 App 本地运行（需 HBuilderX 或 Android Studio 模拟器）
npm run dev:app-android
```

### 后端地址

- **模拟器**：默认 `http://10.0.2.2:8080`（见 `src/utils/api.js`）
- **真机**：登录页填写电脑局域网 IP，如 `http://192.168.1.100:8080`
- **云服务器**：填 `http://你的域名` 或 `https://你的域名`

## 打包 APK（推荐 HBuilderX）

1. 用 [HBuilderX](https://www.dcloud.io/hbuilderx.html) 打开 `apps/uni-client` 目录
2. 菜单：发行 → 原生 App-云打包 → Android
3. 填写应用名称「闪借」、包名（如 `com.zongshe.loan`）
4. 使用 DCloud 公用证书或自有证书
5. 等待云打包完成，下载 APK 安装到手机

## CLI 打包（需本地 Android SDK）

```bash
cd apps/uni-client
npm run build:app-android
```

生成目录通常在 `unpackage/dist/build/app/`。

## 综设 III 交付要求

- 第 9 周：探路 APK，真机能装、能登录  
- 第 11 周：主路径在 APK 上走通，发给组员另一部手机能自己装  
- 第 12 周：终包 `闪借-v2-final.apk`，卸载重装仍可用；答辩用真机安装，不连电脑跑调试基座  
- 只开 H5 浏览器 **不算** 完成老师要求的 Android 借款端  

## 答辩演示建议

1. 云服务器已部署后端 + Nginx（见 `deploy/README.md`）
2. 手机安装终包 APK，登录页填 Gateway / 服务器地址
3. 演示：登录 → 消费贷申请 → 进度（风控）→ 合同/还款 → 工单
