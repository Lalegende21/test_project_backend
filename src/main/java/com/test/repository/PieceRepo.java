package com.test.repository;

import com.test.model.Piece;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PieceRepo extends JpaRepository<Piece, Long> {
}
