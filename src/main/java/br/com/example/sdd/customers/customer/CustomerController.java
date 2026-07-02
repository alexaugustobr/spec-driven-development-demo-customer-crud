package br.com.example.sdd.customers.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Controller
@RequiredArgsConstructor
public class CustomerController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "createdAt");
    private static final Set<String> ALLOWED_DIRECTIONS = Set.of("asc", "desc");
    private static final int PAGE_SIZE = 20;

    private final CustomerRepository customerRepository;

    @GetMapping("/customers")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "createdAt") String sort,
                       @RequestParam(defaultValue = "desc") String dir,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {

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

        return "customers/list";
    }
}
