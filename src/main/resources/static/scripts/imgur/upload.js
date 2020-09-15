var feedback = function(res) {
    if (res.success === true) {
        var get_link = res.data.link.replace(/^http:\/\//i, 'https://');
        document.querySelector('.status').classList.add('bg-success');
       document.getElementById('UserImg').value = get_link;
    }
};

new Imgur({
    clientid: '188b593599c62fb',
    callback: feedback
});