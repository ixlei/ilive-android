'use strict';

const express = require('express');
const app = express();
const bodyParser = require('body-parser');
const session = require('express-session')

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

const userRouter = require('./routes/user.js');

app.use(session({
    secret: 'ilive',
    resave: false,
    saveUninitialized: true,
}));

app.use('/user', userRouter);

app.listen(8001);

module.exports = app;