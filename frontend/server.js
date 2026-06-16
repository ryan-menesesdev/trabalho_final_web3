const express = require('express');
const axios = require('axios');
const path = require('path');

const app = express();
const PORT = 3000;

app.use(express.urlencoded({ extended: true }));
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.post('/send-code', async (req, res) => {
    const { email } = req.body;
    
    try {
        await axios.post('http://localhost:8081/auth/request-code', { email });
        
        res.redirect(`/verify?email=${encodeURIComponent(email)}`);
    } catch (error) {
        console.error("Erro ao solicitar código:", error.message);
        res.status(500).send("Erro ao processar a solicitação. Verifique se o backend está rodando.");
    }
});

app.get('/verify', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'verify.html'));
});

app.post('/verify-code', async (req, res) => {
    const { email, code } = req.body;

    try {
        const response = await axios.post('http://localhost:8081/auth/verify-code', { email, code });

        const token = response.data.token; 

        res.send(`
            <script>
                sessionStorage.setItem('token', '${token}');
                window.location.href = '/register'; 
            </script>
        `);
    } catch (error) {
        console.error("Erro na validação do código:", error.message);
        res.send(`
            <script>
                alert('Código inválido ou expirado. Tente novamente.');
                window.history.back();
            </script>
        `);
    }
});

app.get('/register', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'register.html'));
});

app.get('/dashboard', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'dashboard.html'));
});

app.get('/api/protected', async (req, res) => {
    try {
        const authHeader = req.headers['authorization'];
        
        const response = await axios.get('http://localhost:8081/users/test/customer', {
            headers: { 'Authorization': authHeader }
        });
        
        res.send(response.data);
    } catch (error) {
        res.status(error.response?.status || 500).send(error.response?.data || "Erro de autorização");
    }
});

app.listen(PORT, () => {
    console.log(`Frontend rodando na porta http://localhost:${PORT}`);
});