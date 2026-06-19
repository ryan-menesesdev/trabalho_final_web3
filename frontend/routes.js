const express = require('express');
const axios = require('axios');
const path = require('path');

const router = express.Router();

router.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

router.post('/send-code', async (req, res) => {
    const { email } = req.body;
    
    try {
        await axios.post('http://localhost:8081/auth/request-code', { email });
        res.redirect(`/verify?email=${encodeURIComponent(email)}`);
    } catch (error) {
        console.error(error.message);
        res.status(500).send("Erro ao processar a solicitação. Verifique se o backend está rodando.");
    }
});

router.get('/verify', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'verify.html'));
});

router.post('/verify-code', async (req, res) => {
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
        console.error(error.message);
        res.send(`
            <script>
                alert('Código inválido ou expirado. Tente novamente.');
                window.history.back();
            </script>
        `);
    }
});

router.get('/register', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'register.html'));
});

router.get('/dashboard', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'dashboard.html'));
});

router.get('/api/protected', async (req, res) => {
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

module.exports = router;