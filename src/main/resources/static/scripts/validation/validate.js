    var validator = $("#register").validate({
    rules: {
    password: {
    required: true,
    minlength: 1
},

    passwordCorrect: {
    required: true,
    minlength: 1,
    equalTo: "#password"
}

},
    messages: {
    password: {
    required: "Provide a password",
    minlength: jQuery.validator.format("Enter at least {0} characters")
},
    passwordCorrect: {
    required: "Repeat your password",
    minlength: jQuery.validator.format("Enter at least {0} characters"),
    equalTo: "Passwords do not match. Please try again."
}
},
});
