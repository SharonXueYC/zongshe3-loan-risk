# 如何打开用户端界面

## 方法一：直接打开HTML原型文件（推荐，最简单）

这些HTML文件可以直接在浏览器中打开，无需启动服务器。

**注意：** 这些HTML原型文件是独立的静态文件，**不能通过uni-app开发服务器（localhost:5173）访问**。uni-app开发服务器运行的是Vue组件项目，显示的是默认的"Hello"页面。

### 主要页面文件：

1. **登录页面**：`login-prototype.html`
   - 直接在浏览器中打开：`file:///D:/ZongShe/uni-preset-vue-vite/login-prototype.html`
   - 或双击文件在浏览器中打开

2. **注册页面**：`register-prototype.html`

3. **首页**：`main-home.html`

4. **贷款首页**：`loan-home.html`

5. **贷款提交成功**：`loan-submitted.html`

6. **隐私协议**：`privacy-consent.html`

7. **人脸验证**：`face-verify.html`

8. **银行卡绑定**：`bank-bind.html`

9. **审核进度**：`review-progress.html`

10. **管理员审核等待**：`admin-review-wait.html`

11. **个人资料完成**：`profile/profile-complete.html`

### 快速打开方式：

**Windows系统：**
- 方法1：双击HTML文件，会在默认浏览器中打开
- 方法2：右键点击HTML文件 → 选择"打开方式" → 选择浏览器
- 方法3：在浏览器地址栏输入：`file:///D:/ZongShe/uni-preset-vue-vite/文件名.html`

**推荐入口页面：**
- 首次访问：`login-prototype.html`（登录页面）
- 或：`main-home.html`（首页）

---

## 方法一补充：通过HTTP服务器访问（可选）

如果想通过 `http://localhost` 访问HTML文件（而不是 `file://`），可以使用简单的静态文件服务器：

### 方式1：使用Python（如果已安装）
```bash
cd D:\ZongShe\uni-preset-vue-vite
python -m http.server 8080
```
然后访问：`http://localhost:8080/login-prototype.html`

### 方式2：使用Node.js的http-server
```bash
cd D:\ZongShe\uni-preset-vue-vite
npx http-server -p 8080
```
然后访问：`http://localhost:8080/login-prototype.html`

### 方式3：使用提供的批处理脚本
- Windows：双击 `启动HTML服务器.bat`
- PowerShell：运行 `.\启动HTML服务器.ps1`

---

## 方法二：运行uni-app开发服务器（如果需要完整功能）

如果需要运行完整的uni-app项目（包含Vue组件和路由），需要启动开发服务器：

### 前置要求：安装Node.js

**需要下载的软件：**

1. **Node.js**（包含npm包管理器）
   - 下载地址：https://nodejs.org/zh-cn/
   - 推荐版本：LTS（长期支持版本），如 v20.x 或 v18.x
   - 下载后双击安装，一路点击"下一步"即可
   - 安装完成后会自动包含npm（Node Package Manager）

**验证安装：**
安装完成后，重新打开命令行（PowerShell或CMD），运行以下命令验证：
```bash
node --version
npm --version
```
如果显示版本号，说明安装成功。

### 运行步骤：

1. **打开命令行**（PowerShell或CMD）

2. **进入项目目录**：
   ```bash
   cd D:\ZongShe\uni-preset-vue-vite
   ```

3. **安装依赖**（首次运行需要，可能需要几分钟）：
   ```bash
   npm install
   ```
   > 注意：如果`npm install`很慢，可以使用国内镜像：
   > ```bash
   > npm install --registry=https://registry.npmmirror.com
   > ```

4. **启动开发服务器**：
   ```bash
   npm run dev:h5
   ```

5. **访问地址**：
   - 开发服务器启动后，会显示访问地址，通常是：`http://localhost:5173`
   - 在浏览器中打开该地址即可

### 注意事项：

- 如果遇到端口被占用，Vite会自动使用下一个可用端口
- 修改代码后会自动热更新
- 按 `Ctrl+C` 可以停止开发服务器
- 如果安装依赖时遇到错误，可以尝试删除`node_modules`文件夹后重新安装

---

---

## 推荐方式

**对于快速查看界面效果**：使用方法一，直接打开HTML文件（**无需安装任何软件**）

**对于开发和调试**：使用方法二，运行开发服务器（需要先安装Node.js）

---

## 常见问题

### Q: 为什么`npm install`命令无法运行？
A: 说明您还没有安装Node.js。请先按照上面的步骤安装Node.js，然后重新打开命令行窗口再试。

### Q: 安装Node.js后还是无法运行npm命令？
A: 
1. 确保已重启命令行窗口（关闭后重新打开）
2. 检查环境变量：在命令行输入`where node`，如果显示路径说明安装成功
3. 如果还是不行，可能需要手动添加Node.js到系统PATH环境变量

### Q: npm install时出现很多警告（warn deprecated）？
A: **这是正常的，不影响使用！**
- 这些是"弃用警告"（deprecation warnings），表示某些依赖包使用了旧版本的库
- 只要看到"added XXX packages"就说明安装成功了
- 这些警告不会影响项目运行，可以安全忽略
- 只有看到"error"才是真正的错误，需要处理

### Q: 我只想查看界面，不想安装软件怎么办？
A: 直接使用方法一，双击HTML文件在浏览器中打开即可，**完全不需要安装任何软件**。

### Q: npm install很慢怎么办？
A: 使用国内镜像源：
```bash
npm install --registry=https://registry.npmmirror.com
```

### Q: npm提示有新版本可用？
A: （可选，不是必须的）如果想更新npm到最新版本：
```bash
npm install -g npm@latest
```
不更新也不影响项目使用。

### Q: 如何卸载Node.js？
A: 在Windows控制面板 → 程序和功能 → 卸载Node.js即可

