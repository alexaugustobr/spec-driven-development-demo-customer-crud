(function () {
    var zipCodeInput = document.getElementById('zipCode');
    if (!zipCodeInput) return;

    zipCodeInput.addEventListener('input', function () {
        var value = this.value.replace(/\D/g, '');

        if (value.length >= 5) {
            value = value.substring(0, 5) + '-' + value.substring(5, 8);
        }
        this.value = value;

        var digits = value.replace(/\D/g, '');
        if (digits.length === 8) {
            showSpinner();
            fetch('/api/cep/' + digits)
                .then(function (response) {
                    if (response.ok) return response.json();
                    throw new Error('not found');
                })
                .then(function (data) {
                    fillField('street', data.street);
                    fillField('neighborhood', data.neighborhood);
                    fillField('city', data.city);
                    fillField('state', data.state);
                })
                .catch(function () {
                    // leave fields editable on failure
                })
                .finally(function () {
                    hideSpinner();
                });
        }
    });

    function showSpinner() {
        ['street', 'neighborhood', 'city', 'state'].forEach(setLoading);
    }

    function setLoading(id) {
        var el = document.getElementById(id);
        if (el) {
            el.dataset.prevPlaceholder = el.placeholder;
            el.placeholder = 'Carregando...';
            el.readOnly = true;
        }
    }

    function hideSpinner() {
        ['street', 'neighborhood', 'city', 'state'].forEach(resetField);
    }

    function resetField(id) {
        var el = document.getElementById(id);
        if (el) {
            el.placeholder = el.dataset.prevPlaceholder || '';
            el.readOnly = false;
        }
    }

    function fillField(id, value) {
        var el = document.getElementById(id);
        if (el && value) {
            el.value = value;
        }
    }
})();