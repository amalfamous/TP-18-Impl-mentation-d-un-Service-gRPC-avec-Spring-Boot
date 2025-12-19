package org.example.tp18_grbc.repositories;


import org.example.tp18_grbc.entities.Compte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CompteRepository extends JpaRepository<Compte, String> {

    // Requête JPQL pour la somme des soldes (retourne null si aucun compte)
    @Query("SELECT COALESCE(SUM(c.solde), 0) FROM Compte c")
    float getSumSolde();
}