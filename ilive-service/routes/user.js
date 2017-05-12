'use script';
const express = require('express');
const Router = express.Router();

const userDb = require('../models/user.js');

Router.post("/login", (req, res, next) => {
    const { nickname, password } = req.body;
    userDb.checkUser(nickname)
        .then((data) => {
            res.json(data);
        }, (err) => {
            res.json({
                ret: 1,
                result: 'database error',
            });
        });
});

Router.post("/register", (req, res, next) => {
    const { nickname, password } = req.body;

    userDb.registered(nickname, password)
        .then((result) => {
            res.json("success");
            console.log(JSON.stringify(result));
        }, (err) => {
            res.json(JSON.stringify(err));
            console.log(JSON.stringify(err));
        });
});

module.exports = Router;