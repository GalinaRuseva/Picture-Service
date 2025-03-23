package app.repository;

import app.model.Picture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PictureRepository extends JpaRepository<Picture, UUID> {

}
