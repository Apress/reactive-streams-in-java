

<form action="/pay" method="POST">
    <script
            src="https://checkout.stripe.com/checkout.js"
            class="stripe-button"
            data-key="${dataKey}"
            data-image="/images/${image}"
            data-name="${name}"
            data-description="${description}"
            data-amount="${price}">
    </script>
</form>
