# Frontend

Интерфейс Farm RPG. В разработке страница открывается отдельно от Spring: запросы на `/auth` Vite пересылает на `http://localhost:8080`.

Сборка на Vite 6, потому что сейчас установлен Node.js 20.17. Vite 8 требует Node.js 20.19 или новее.

```bash
cd frontend
npm install
npm run dev
```

Страницы входа и регистрации: http://localhost:5173
