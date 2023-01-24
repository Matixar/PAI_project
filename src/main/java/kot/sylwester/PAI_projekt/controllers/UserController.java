package kot.sylwester.PAI_projekt.controllers;

import kot.sylwester.PAI_projekt.dao.NoteDao;
import kot.sylwester.PAI_projekt.dao.UserDao;
import kot.sylwester.PAI_projekt.entities.Note;
import kot.sylwester.PAI_projekt.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Date;

@Controller
public class UserController {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserDao dao;
    @Autowired
    private NoteDao noteDao;
    @GetMapping("/login")
    public String loginPage() {
        //zwrócenie nazwy widoku logowania - login.html
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model m) {
        //dodanie do modelu nowego użytkownika
        m.addAttribute("user", new User());
        //zwrócenie nazwy widoku rejestracji - register.html
        return "register";
    }
    @PostMapping("/register")
    public String registerPagePOST(@Valid User user, BindingResult binding) {
        if (binding.hasErrors()) {
            return "register"; //powrót do rejestracji
        }
        if(dao.findByLogin(user.getLogin()) != null) {
            binding.rejectValue("login", "500", "Login already exists");
            return "register";
        }
        if(user.getLogin().isEmpty()) {
            binding.rejectValue("login", "500", "Login cannot be empty");
            return "register";
        }
        if(user.getPassword().isEmpty()) {
            binding.rejectValue("password", "500", "Password cannot be empty");
            return "register";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        dao.save(user);
        //przekierowanie do adresu url: /login
        return "redirect:/login";
    }
    @GetMapping("/profile")
    public String profilePage(Model m, Principal principal) {
        //dodanie do modelu aktualnie zalogowanego użytkownika:
        m.addAttribute("user", dao.findByLogin(principal.getName()));
        m.addAttribute("note",noteDao.findFirstByUser_IdOrderByCreationDateDesc(dao.findByLogin(principal.getName()).getId()));
        //zwrócenie nazwy widoku profilu użytkownika - profile.html
        return "profile";
    }

    @GetMapping("/mynotes")
    public String myNotesPage(Model m, Principal principal) {
        m.addAttribute("notes",noteDao.findAllByUser_Id(dao.findByLogin(principal.getName()).getId()));
        return "mynotes";
    }

    @GetMapping("/allnotes")
    public String allNotesPage(Model m, Principal principal) {
        m.addAttribute("notes", noteDao.findAll());
        m.addAttribute("user",dao.findByLogin(principal.getName()));
        return "allnotes";
    }

    @GetMapping("/editnote/{id}")
    public String editNotePage(Model m, @PathVariable Integer id, Principal principal) {
        if(noteDao.findById(id).isPresent()) {
            if (noteDao.findById(id).get().getUser().getId() != dao.findByLogin(principal.getName()).getId())
                return "redirect:/mynotes";
            m.addAttribute("note", noteDao.findById(id).get());
            return "editnote";
        }
        else return "redirect:/mynotes";
    }

    @PostMapping(value = "/editnote/{id}", params = "edit")
    public String editNotePagePOST(@Valid Note note, BindingResult binding, @PathVariable Integer id) {
        if (binding.hasErrors()) {
            return "editnote";
        }
        Note originalNote = noteDao.findById(id).get();
        originalNote.setLastModifiedDate(new Date());
        originalNote.setTitle(note.getTitle());
        originalNote.setMessage(note.getMessage());
        noteDao.save(originalNote);
        return "redirect:/mynotes";
    }

    @PostMapping(value = "/editnote/{id}", params = "delete")
    public String editNotePageDELETE(@Valid Note note, @PathVariable Integer id) {
        noteDao.deleteById(id);
        return "redirect:/mynotes";
    }

    @GetMapping("/edit")
    public String editPage(Model m, Principal principal) {
        m.addAttribute("user",dao.findByLogin(principal.getName()));
        return "edit";
    }

    @PostMapping("/edit")
    public String editPagePOST(@Valid User user, BindingResult binding, Principal principal) {
        if (binding.hasErrors()) {
            return "edit"; //powrót do edycji
        }
        if(dao.findByLogin(user.getLogin()) != null) {
            User u = dao.findByLogin(user.getLogin());
            if(u.getId() != dao.findByLogin(principal.getName()).getId()) {
                binding.rejectValue("login", "500", "Podany login już istnieje");
                return "edit";
            }
        }
        user.setId(dao.findByLogin(principal.getName()).getId());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        dao.save(user);
        return "redirect:/logout";
    }
    @GetMapping("/addnote")
    public String addNotePage(Model m) {
        m.addAttribute("note", new Note());
        return "addnote";
    }
    @PostMapping("/addnote")
    public String addNotePagePOST(@Valid Note note, BindingResult binding, Principal principal) {
        if (binding.hasErrors()) {
            return "addnote";
        }
        if(note.getMessage().isEmpty()) {
            binding.rejectValue("message", "500", "Message cannot be empty");
            return "addnote";
        }
        if(note.getTitle().isEmpty()) {
            binding.rejectValue("title", "500", "Title cannot be empty");
            return "addnote";
        }
        note.setUser(dao.findByLogin(principal.getName()));
        note.setCreationDate(new Date());
        note.setLastModifiedDate(new Date());
        noteDao.save(note);
        return "redirect:/profile";
    }
}
