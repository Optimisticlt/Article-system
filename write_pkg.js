const fs = require('fs');
const path = require('path');
const pkg = {
  name: "user-login-frontend",
  version: "1.0.0",
  private: true,
  type: "module",
  scripts: {
    dev: "vite",
    build: "vite build",
    preview: "vite preview"
  },
  dependencies: {
    vue: "^3.4.21",
    "vue-router": "^4.3.0",
    axios: "^1.6.7",
    "element-plus": "^2.5.6",
    "@element-plus/icons-vue": "^2.3.1"
  },
  devDependencies: {
    "@vitejs/plugin-vue": "^5.0.4",
    vite: "^5.1.4"
  }
};

const pkgPath = path.join(__dirname, 'frontend', 'package.json');
fs.writeFileSync(pkgPath, JSON.stringify(pkg, null, 2) + '\n', 'utf8');
console.log('Written to:', pkgPath);
console.log('Content:', fs.readFileSync(pkgPath, 'utf8').substring(0, 100));
