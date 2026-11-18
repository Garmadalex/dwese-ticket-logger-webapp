package org.alixar.daw2.Alejandro.dwese_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.alixar.daw2.Alejandro.dwese_ticket_logger_webapp.repositories.RegionRepository;
import org.alixar.daw2.Alejandro.dwese_ticket_logger_webapp.entities.Region;
import org.alixar.daw2.Alejandro.dwese_ticket_logger_webapp.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
@RequestMapping("/regions")
public class RegionController {

    private static final Logger logger = LoggerFactory.getLogger(RegionController.class);

    private final RegionRepository regionRepository;
    private final MessageSource messageSource;
    private final FileStorageService fileStorageService;

    public RegionController(RegionRepository regionRepository, MessageSource messageSource,
                            FileStorageService fileStorageService) {
        this.regionRepository = regionRepository;
        this.messageSource = messageSource;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String listRegions(Model model, Locale locale) {
        logger.info("Solicitando la lista de todas las regiones...");
        List<Region> listRegions = null;
        try {
            listRegions = regionRepository.findAll();
            logger.info("Se han cargado {} regiones.", listRegions.size());
        } catch (DataAccessException e) {
            logger.error("Error al listar las regiones: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error al listar las regiones.");
        }
        model.addAttribute("listRegions", listRegions);
        return "region";
    }

    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nueva región.");
        model.addAttribute("region", new Region());
        return "region-form";
    }

    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model,
                               RedirectAttributes redirectAttributes, Locale locale) {
        logger.info("Mostrando formulario de edición para la región con ID {}", id);
        try {
            Optional<Region> region = regionRepository.findById(id);
            if (region.isEmpty()) {
                logger.warn("No se encontró la región con ID {}", id);
                redirectAttributes.addFlashAttribute("errorMessage",
                        messageSource.getMessage("msg.region-controller.edit.notFound", null, locale));
                return "redirect:/regions";
            }
            model.addAttribute("region", region.get());
        } catch (DataAccessException e) {
            logger.error("Error al obtener la región con ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.region-controller.edit.error", null, locale));
            return "redirect:/regions";
        }
        return "region-form";
    }

    @PostMapping("/insert")
    public String insertRegion(@Valid @ModelAttribute("region") Region region, BindingResult result,
                               @RequestParam("imageFile") MultipartFile imageFile,
                               RedirectAttributes redirectAttributes, Locale locale) {
        logger.info("Insertando nueva región con código {}", region.getCode());
        try {
            if (result.hasErrors()) {
                return "region-form";
            }
            if (regionRepository.existsRegionByCode(region.getCode())) {
                logger.warn("El código de la región {} ya existe.", region.getCode());
                result.rejectValue("code", "msg.region-controller.insert.codeExist");
                return "region-form";
            }
            if (!imageFile.isEmpty()) {
                String fileName = fileStorageService.saveFile(imageFile);
                if (fileName != null) {
                    region.setImage(fileName);
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            messageSource.getMessage("msg.region-form.image.invalid", null, locale));
                    return "redirect:/regions";
                }
            }
            regionRepository.save(region);
            logger.info("Región {} insertada con éxito.", region.getCode());
        } catch (DataAccessException e) {
            logger.error("Error al insertar la región {}: {}", region.getCode(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.region-controller.insert.error", null, locale));
        }
        return "redirect:/regions";
    }

    @PostMapping("/update")
    public String updateRegion(@Valid @ModelAttribute("region") Region region, BindingResult result,
                               @RequestParam("imageFile") MultipartFile imageFile,
                               RedirectAttributes redirectAttributes, Locale locale) {
        logger.info("Actualizando región con ID {}", region.getId());
        try {
            if (result.hasErrors()) {
                return "region-form";
            }
            if (regionRepository.existsRegionByCodeAndNotId(region.getCode(), region.getId())) {
                logger.warn("El código de la región {} ya existe para otra región.", region.getCode());
                result.rejectValue("code", "msg.region-controller.update.codeExist");
                return "region-form";
            }
            if (!imageFile.isEmpty()) {
                String fileName = fileStorageService.saveFile(imageFile);
                if (fileName != null) {
                    region.setImage(fileName);
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            messageSource.getMessage("msg.region-form.image.invalid", null, locale));
                    return "redirect:/regions";
                }
            }
            regionRepository.save(region);
            logger.info("Región con ID {} actualizada con éxito.", region.getId());
        } catch (DataAccessException e) {
            logger.error("Error al actualizar la región con ID {}: {}", region.getId(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.region-controller.update.error", null, locale));
        }
        return "redirect:/regions";
    }

    @PostMapping("/delete")
    public String deleteRegion(@RequestParam("id") Long id, RedirectAttributes redirectAttributes, Locale locale) {
        logger.info("Eliminando región con ID {}", id);
        try {
            Optional<Region> regionOpt = regionRepository.findById(id);
            if (regionOpt.isPresent()) {
                String image = regionOpt.get().getImage();
                regionRepository.deleteById(id);
                // La imagen se borra solo después de borrar la región con éxito
                if (image != null && !image.isEmpty()) {
                    fileStorageService.deleteFile(image);
                }
                logger.info("Región con ID {} eliminada con éxito.", id);
            } else {
                logger.warn("No se encontró la región con ID {} para eliminar.", id);
            }
        } catch (DataIntegrityViolationException e) {
            logger.warn("No se puede eliminar la región con ID {}: tiene provincias asociadas.", id);
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.region-controller.delete.hasProvinces", null, locale));
        } catch (DataAccessException e) {
            logger.error("Error al eliminar la región con ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.region-controller.delete.error", null, locale));
        }
        return "redirect:/regions";
    }
}