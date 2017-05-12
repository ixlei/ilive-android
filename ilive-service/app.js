'use strict';

const express = require('express');
const app = express();
const bodyParser = require('body-parser');

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

const userRouter = require('./routes/user.js');

app.use('/user', userRouter);

app.listen(8001);

console.log(typeof require('./config'));
module.exports = app;