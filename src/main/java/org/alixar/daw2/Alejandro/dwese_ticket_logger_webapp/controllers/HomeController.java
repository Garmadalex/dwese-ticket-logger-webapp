package org.alixar.daw2.Alejandro.dwese_ticket_logger_webapp.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    /**
     * Maneja la ruta raíz de la aplicación para mostrar la página de inicio.
     * @return El nombre de la plantilla Thymeleaf (index.html).
     */
    @GetMapping("/")
    public String home() {
        logger.info("Accediendo a la página de inicio (Home)...");
        return "index"; // Renderiza la vista index.html ubicada en src/main/resources/templates/
    }
}
