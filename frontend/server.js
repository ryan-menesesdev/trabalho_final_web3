const express = require('express');
const path = require('path');
const routes = require('./routes');

const app = express();
const PORT = 3000;

app.use(express.urlencoded({ extended: true }));
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', routes);

app.listen(PORT, () => {
    console.log(`Frontend rodando na porta http://localhost:${PORT}`);
});