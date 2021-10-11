import axios from 'axios';
import fetch from 'node-fetch';
import bent from 'bent';
import superagent from 'superagent';
import needle from 'needle';
import got from 'got';

const host = process.env.HOST ?? 'localhost:8080';

async function init() {
    console.log('Sending requests, this might take a while');
    await Promise.all(
        [
            time('axios', callWithAxios),
            time('axios with timeout', () => callWithAxiosTimeout(10000)),
            time('fetch', callWithFetch),
            time('bent', callWithBent),
            time('superagent', callWithSuperAgent),
            time('superagent with timeout', () => callWithSuperAgentTimeout(10000)),
            time('needle', callWithNeedle),
            time('needle with timeout', () => callWithNeedleTimeout(10000)),
            time('got', callWithGot),
            time('got with timeout', () => callWithGotTimeout(10000)),
        ]
    )
    console.log('All done')
}

async function time(tag, func) {
    const before = Date.now();
    try {
        await func();
        console.log(`${tag} request took ${(Date.now() - before) / 1000} seconds`);
    } catch {
        console.warn(`${tag} failed after ${(Date.now() - before) / 1000} seconds`);
    }
}

async function callWithAxios() {
    await axios.post(`http://${host}/`);
}

async function callWithAxiosTimeout(timeout) {
    await axios.post(`http://${host}/`, { timeout });
}

async function callWithFetch() {
    await fetch(`http://${host}/`, { method: 'POST' });
}

async function callWithBent() {
    await bent(`http://${host}/`, 'POST', 'json')();
}

async function callWithSuperAgent() {
    await superagent.post(`http://${host}/`);
}

async function callWithSuperAgentTimeout(timeout) {
    await superagent.post(`http://${host}/`).timeout({
        deadline: timeout
    });
}

async function callWithNeedle() {
    await needle('post', `http://${host}/`);
}

async function callWithNeedleTimeout(timeout) {
    await needle('post', `http://${host}/`, {}, {
        read_timeout: timeout,
        response_timeout: timeout
    });
}

async function callWithGot() {
    await got.post(`http://${host}/`).json();
}

async function callWithGotTimeout(timeout) {
    await got.post(`http://${host}/`, { timeout }).json();
}

init();
