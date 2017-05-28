'use strict';

const express = require('express');
const app = express();
const ejs = require('ejs');
const path = require('path');
const morgan = require('morgan');
const bodyParser = require('body-parser');
const session = require('express-session')

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');


const userRouter = require('./routes/user.js');
const live = require('./routes/live.js');

//logger
app.use(morgan('combined'));

//session
app.use(session({
    secret: 'ilive',
    resave: false,
    saveUninitialized: true,
}));

app.use('/user', userRouter);
app.use('/live', live);

app.listen(8001);

module.exports = app;