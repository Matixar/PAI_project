package kot.sylwester.PAI_projekt.dao;

import kot.sylwester.PAI_projekt.entities.Note;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NoteDao extends CrudRepository<Note, Integer> {
    public List<Note> findAllByUser_Id(Integer user_id);

    public Note findFirstByUser_IdOrderByCreationDateDesc(Integer user_id);
}
