package br.com.example.sdd.customers.customer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

@Controller
@RequiredArgsConstructor
public class CustomerController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "createdAt");
    private static final Set<String> ALLOWED_DIRECTIONS = Set.of("asc", "desc");
    private static final int PAGE_SIZE = 20;

    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    @GetMapping("/customers")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "createdAt") String sort,
                       @RequestParam(defaultValue = "desc") String dir,
                       @RequestParam(defaultValue = "0") int page,
                       Model model,
                       Authentication authentication) {

        if (!ALLOWED_SORT_FIELDS.contains(sort)) {
            sort = "createdAt";
        }
        Sort.Direction direction = ALLOWED_DIRECTIONS.contains(dir) ? Sort.Direction.fromString(dir) : Sort.Direction.DESC;
        String resolvedDir = direction.isAscending() ? "asc" : "desc";

        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(direction, sort));

        Page<Customer> customerPage;
        if (q == null || q.isBlank()) {
            customerPage = customerRepository.findAll(pageRequest);
        } else {
            q = q.trim();
            if (q.matches("\\d{11}|\\d{14}")) {
                customerPage = customerRepository.findByDocument(q, pageRequest);
            } else if (q.contains("@")) {
                customerPage = customerRepository.findByEmailContainingIgnoreCase(q, pageRequest);
            } else {
                customerPage = customerRepository.findByNameContainingIgnoreCase(q, pageRequest);
            }
        }

        model.addAttribute("page", customerPage);
        model.addAttribute("q", q != null ? q : "");
        model.addAttribute("sort", sort);
        model.addAttribute("dir", resolvedDir);
        model.addAttribute("isAdmin", isAdmin(authentication));

        return "customers/list";
    }

    @GetMapping("/customers/new")
    public String newForm(Model model, Authentication authentication) {
        model.addAttribute("customer", new Customer());
        model.addAttribute("isAdmin", isAdmin(authentication));
        return "customers/form";
    }

    @PostMapping("/customers")
    public String create(@Valid @ModelAttribute Customer customer,
                          BindingResult result,
                          RedirectAttributes redirectAttributes,
                          Authentication authentication,
                          Model model) {
        model.addAttribute("isAdmin", isAdmin(authentication));

        if (result.hasErrors()) {
            return "customers/form";
        }

        try {
            customerService.create(customer);
        } catch (IllegalArgumentException e) {
            result.reject("error.customer", e.getMessage());
            return "customers/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Cliente criado com sucesso.");
        return "redirect:/customers";
    }

    @GetMapping("/customers/{id}")
    public String view(@PathVariable Long id, Model model, Authentication authentication) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("customer", customer);
        model.addAttribute("isAdmin", isAdmin(authentication));
        return "customers/view";
    }

    @GetMapping("/customers/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, Authentication authentication) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("customer", customer);
        model.addAttribute("isAdmin", isAdmin(authentication));
        return "customers/form";
    }

    @PostMapping("/customers/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute Customer customer,
                          BindingResult result,
                          RedirectAttributes redirectAttributes,
                          Authentication authentication,
                          Model model) {
        model.addAttribute("isAdmin", isAdmin(authentication));

        if (result.hasErrors()) {
            return "customers/form";
        }

        try {
            customerService.update(id, customer);
        } catch (IllegalArgumentException e) {
            result.reject("error.customer", e.getMessage());
            return "customers/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Cliente atualizado com sucesso.");
        return "redirect:/customers/" + id;
    }

    @PostMapping("/customers/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        customerService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Cliente exclu\u00EDdo com sucesso.");
        return "redirect:/customers";
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
