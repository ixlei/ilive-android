'use strict';

const express = require('express');
const app = express();

const userRouter = require('./routes/user.js');

app.use('/user', userRouter);

app.listen(8001);

console.log(typeof require('./config'));
module.exports = app;