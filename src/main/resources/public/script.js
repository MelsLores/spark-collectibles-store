// ============================================
// EJERCICIO 1: PLANTILLAS - Navegación de Items
// ============================================
// JavaScript mejorado para navegación con Bootstrap

document.addEventListener('DOMContentLoaded', function() {
    // Smooth scroll to top on navigation
    window.scrollTo({ top: 0, behavior: 'smooth' });
    
    // Add loading animation to item cards
    const itemCards = document.querySelectorAll('.hover-card');
    itemCards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        setTimeout(() => {
            card.style.transition = 'all 0.5s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 100);
    });
    
    // Enhanced click handling for item links
    var itemListTable = document.querySelector('#item-list-table');
    if (itemListTable) {
        itemListTable.addEventListener('click', function(event) {
            if (event.target.matches('a.item-name-link')) {
                event.preventDefault();
                
                // Add loading effect
                const button = event.target;
                const originalText = button.innerHTML;
                button.innerHTML = '<i class="bi bi-hourglass-split"></i> Loading...';
                button.classList.add('disabled');
                
                setTimeout(() => {
                    var itemId = button.getAttribute('href').split('/')[2];
                    window.location.href = '/items/' + itemId;
                }, 300);
            }
        });
    }
    
    // Add ripple effect to buttons
    const buttons = document.querySelectorAll('.btn');
    buttons.forEach(button => {
        button.addEventListener('click', function(e) {
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;
            
            ripple.style.width = ripple.style.height = size + 'px';
            ripple.style.left = x + 'px';
            ripple.style.top = y + 'px';
            ripple.classList.add('ripple');
            
            this.appendChild(ripple);
            
            setTimeout(() => ripple.remove(), 600);
        });
    });
    
    // Animate badges on page load
    const badges = document.querySelectorAll('.badge');
    badges.forEach((badge, index) => {
        badge.style.opacity = '0';
        badge.style.transform = 'scale(0.8)';
        setTimeout(() => {
            badge.style.transition = 'all 0.3s ease';
            badge.style.opacity = '1';
            badge.style.transform = 'scale(1)';
        }, 200 + (index * 50));
    });
    
    // Add tooltip functionality (Bootstrap tooltips)
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip) {
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }
    
    // Scroll animations for elements
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate-in');
            }
        });
    }, observerOptions);
    
    document.querySelectorAll('.card, .alert, .table').forEach(el => {
        observer.observe(el);
    });
});


// ====================================================
// EJERCICIO 2: FORMULARIOS - Manejo de Ofertas
// ====================================================
// jQuery mejorado para manejo de formulario de ofertas con mejor UX

$(document).ready(function() {
    // Toggle offer form with animation
    $(".offer-btn").click(function() {
        $("#offer-form").slideToggle(300);
    });

    // Real-time email validation
    $("#email-input").on('blur', function() {
        const email = $(this).val();
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        
        if (email && !emailRegex.test(email)) {
            $(this).addClass('is-invalid');
            $(this).removeClass('is-valid');
        } else if (email) {
            $(this).addClass('is-valid');
            $(this).removeClass('is-invalid');
        }
    });
    
    // Real-time name validation
    $("#name-input").on('blur', function() {
        const name = $(this).val().trim();
        
        if (name.length < 2) {
            $(this).addClass('is-invalid');
            $(this).removeClass('is-valid');
        } else if (name) {
            $(this).addClass('is-valid');
            $(this).removeClass('is-invalid');
        }
    });
    
    // Real-time amount validation
    $("#amount-input").on('input', function() {
        const amount = parseFloat($(this).val());
        
        if (amount <= 0 || isNaN(amount)) {
            $(this).addClass('is-invalid');
            $(this).removeClass('is-valid');
        } else {
            $(this).addClass('is-valid');
            $(this).removeClass('is-invalid');
        }
    });

    // Enhanced form submission with better UX
    $("#offer-form").submit(function(event) {
        event.preventDefault();

        let name = $("#name-input").val().trim();
        let email = $("#email-input").val().trim();
        let amount = $("#amount-input").val();

        // Hide previous messages
        $("#error-message").addClass("d-none");
        $("#success-message").addClass("d-none");

        // Client-side validation
        if (!name || !email || !amount) {
            $("#error-text").text("Please fill out all fields!");
            $("#error-message").removeClass("d-none");
            
            // Shake animation for error
            $("#error-message").addClass('shake');
            setTimeout(() => $("#error-message").removeClass('shake'), 500);
            return;
        }

        if (parseFloat(amount) <= 0) {
            $("#error-text").text("Amount must be greater than 0!");
            $("#error-message").removeClass("d-none");
            $("#error-message").addClass('shake');
            setTimeout(() => $("#error-message").removeClass('shake'), 500);
            return;
        }

        // Disable form during submission
        $("button[type='submit']").prop("disabled", true)
            .html('<span class="spinner-border spinner-border-sm me-2"></span>Submitting...');
        $("#offer-form :input").prop("disabled", true);

        // Prepare offer data
        let offerData = {
            name: name,
            email: email,
            amount: parseFloat(amount)
        };

        // Submit via AJAX
        $.ajax({
            url: "/api/offer",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify(offerData),
            success: function(response) {
                $("#success-text").text("Your offer has been submitted successfully! 🎉");
                $("#success-message").removeClass("d-none");
                
                // Confetti effect (simple)
                createConfetti();
                
                $("#offer-form").trigger("reset");
                $("#offer-form :input").removeClass('is-valid is-invalid');
                
                // Redirect after delay
                setTimeout(() => {
                    window.location.href = "/offers/list";
                }, 2000);
            },
            error: function(jqXHR, textStatus, errorThrown) {
                let errorMsg = "Error submitting offer";
                if (jqXHR.responseJSON && jqXHR.responseJSON.message) {
                    errorMsg = jqXHR.responseJSON.message;
                } else if (jqXHR.responseText) {
                    errorMsg = jqXHR.responseText;
                }
                
                $("#error-text").text(errorMsg);
                $("#error-message").removeClass("d-none");
                $("#error-message").addClass('shake');
                setTimeout(() => $("#error-message").removeClass('shake'), 500);
                
                // Re-enable form
                $("button[type='submit']").prop("disabled", false)
                    .html('<i class="bi bi-send"></i> Submit Offer');
                $("#offer-form :input").prop("disabled", false);
            }
        });
    });
    
    // Simple confetti effect
    function createConfetti() {
        const colors = ['#0d6efd', '#198754', '#ffc107', '#dc3545', '#0dcaf0'];
        for (let i = 0; i < 50; i++) {
            setTimeout(() => {
                const confetti = $('<div class="confetti"></div>');
                confetti.css({
                    left: Math.random() * 100 + '%',
                    backgroundColor: colors[Math.floor(Math.random() * colors.length)],
                    animationDelay: Math.random() * 0.5 + 's'
                });
                $('body').append(confetti);
                setTimeout(() => confetti.remove(), 3000);
            }, i * 20);
        }
    }
    
    // Auto-format amount input
    $("#amount-input").on('change', function() {
        const value = parseFloat($(this).val());
        if (!isNaN(value)) {
            $(this).val(value.toFixed(2));
        }
    });
    
    // Character counter for name (optional enhancement)
    $("#name-input").on('input', function() {
        const length = $(this).val().length;
        if (length > 0) {
            $(this).attr('placeholder', length + ' characters');
        } else {
            $(this).attr('placeholder', 'Enter your full name');
        }
    });
});

// Add loading spinner when navigating
window.addEventListener('beforeunload', function() {
    document.body.style.opacity = '0.5';
    document.body.style.pointerEvents = 'none';
});

