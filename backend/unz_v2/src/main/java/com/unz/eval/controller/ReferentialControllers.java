package com.unz.eval.controller;

import com.unz.eval.dto.DTOs;
import com.unz.eval.entity.*;
import com.unz.eval.exception.BadRequestException;
import com.unz.eval.exception.ResourceNotFoundException;
import com.unz.eval.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CRUD du référentiel académique (v3) — jusqu'ici absent de l'API.
 * "Administrateur gère les comptes utilisateurs, les classes et les matières"
 * (tableau des acteurs, cahier des charges) : ADMIN uniquement, sauf mention contraire.
 */

// ══════════════════════════════════════════════════════════════════════════
// FILIÈRE
// ══════════════════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/admin/filieres")
@PreAuthorize("hasRole('ADMIN')")
class FiliereController {

    private final FiliereRepository filiereRepo;

    public FiliereController(FiliereRepository filiereRepo) {
        this.filiereRepo = filiereRepo;
    }

    @GetMapping
    public ResponseEntity<List<DTOs.FiliereDTO>> list() {
        return ResponseEntity.ok(filiereRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<DTOs.FiliereDTO> create(@Valid @RequestBody DTOs.CreateFiliereRequest req) {
        if (filiereRepo.findByCode(req.getCode()).isPresent())
            throw new BadRequestException("Ce code de filière existe déjà.");
        Filiere f = filiereRepo.save(Filiere.builder().nom(req.getNom()).code(req.getCode()).build());
        return ResponseEntity.status(201).body(toDTO(f));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        filiereRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private DTOs.FiliereDTO toDTO(Filiere f) {
        return DTOs.FiliereDTO.builder().id(f.getId()).nom(f.getNom()).code(f.getCode()).build();
    }
}

// ══════════════════════════════════════════════════════════════════════════
// NIVEAU
// ══════════════════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/admin/niveaux")
@PreAuthorize("hasRole('ADMIN')")
class NiveauController {

    private final NiveauRepository niveauRepo;
    private final FiliereRepository filiereRepo;

    public NiveauController(NiveauRepository niveauRepo, FiliereRepository filiereRepo) {
        this.niveauRepo = niveauRepo;
        this.filiereRepo = filiereRepo;
    }

    @GetMapping
    public ResponseEntity<List<DTOs.NiveauDTO>> list(@RequestParam(required = false) Long filiereId) {
        List<Niveau> niveaux = filiereId != null
                ? niveauRepo.findByFiliere(filiereRepo.findById(filiereId)
                    .orElseThrow(() -> new ResourceNotFoundException("Filière introuvable")))
                : niveauRepo.findAll();
        return ResponseEntity.ok(niveaux.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<DTOs.NiveauDTO> create(@Valid @RequestBody DTOs.CreateNiveauRequest req) {
        Filiere filiere = filiereRepo.findById(req.getFiliereId())
                .orElseThrow(() -> new ResourceNotFoundException("Filière introuvable"));
        Niveau n = niveauRepo.save(Niveau.builder().libelle(req.getLibelle()).filiere(filiere).build());
        return ResponseEntity.status(201).body(toDTO(n));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        niveauRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private DTOs.NiveauDTO toDTO(Niveau n) {
        return DTOs.NiveauDTO.builder().id(n.getId()).libelle(n.getLibelle())
                .filiereNom(n.getFiliere().getNom()).build();
    }
}

// ══════════════════════════════════════════════════════════════════════════
// ANNÉE ACADÉMIQUE
// ══════════════════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/admin/annees")
@PreAuthorize("hasRole('ADMIN')")
class AnneeAcademiqueController {

    private final AnneeAcademiqueRepository anneeRepo;

    public AnneeAcademiqueController(AnneeAcademiqueRepository anneeRepo) {
        this.anneeRepo = anneeRepo;
    }

    @GetMapping
    public ResponseEntity<List<DTOs.AnneeAcademiqueDTO>> list() {
        return ResponseEntity.ok(anneeRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<DTOs.AnneeAcademiqueDTO> create(@Valid @RequestBody DTOs.CreateAnneeRequest req) {
        if (anneeRepo.findByLibelle(req.getLibelle()).isPresent())
            throw new BadRequestException("Cette année académique existe déjà.");
        AnneeAcademique a = anneeRepo.save(AnneeAcademique.builder()
                .libelle(req.getLibelle()).dateDebut(req.getDateDebut()).dateFin(req.getDateFin())
                .active(false).build());
        return ResponseEntity.status(201).body(toDTO(a));
    }

    /**
     * Active une année académique et désactive automatiquement les autres —
     * une seule année active à la fois (c'est elle qui détermine la classe
     * courante de chaque étudiant via Inscription).
     */
    @PostMapping("/{id}/activer")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<DTOs.AnneeAcademiqueDTO> activer(@PathVariable Long id) {
        AnneeAcademique cible = anneeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Année académique introuvable"));
        anneeRepo.findByActiveTrue().ifPresent(a -> { a.setActive(false); anneeRepo.save(a); });
        cible.setActive(true);
        anneeRepo.save(cible);
        return ResponseEntity.ok(toDTO(cible));
    }

    private DTOs.AnneeAcademiqueDTO toDTO(AnneeAcademique a) {
        return DTOs.AnneeAcademiqueDTO.builder()
                .id(a.getId()).libelle(a.getLibelle())
                .dateDebut(a.getDateDebut()).dateFin(a.getDateFin()).active(a.isActive()).build();
    }
}

// ══════════════════════════════════════════════════════════════════════════
// CLASSE
// ══════════════════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/admin/classes")
@PreAuthorize("hasRole('ADMIN')")
class ClasseController {

    private final ClasseRepository classeRepo;
    private final NiveauRepository niveauRepo;
    private final AnneeAcademiqueRepository anneeRepo;

    public ClasseController(ClasseRepository classeRepo, NiveauRepository niveauRepo, AnneeAcademiqueRepository anneeRepo) {
        this.classeRepo = classeRepo;
        this.niveauRepo = niveauRepo;
        this.anneeRepo = anneeRepo;
    }

    @GetMapping
    public ResponseEntity<List<DTOs.ClasseDTO>> list(@RequestParam(required = false) Long niveauId,
                                                       @RequestParam(required = false) Long anneeId) {
        List<Classe> classes;
        if (niveauId != null) {
            classes = classeRepo.findByNiveau(niveauRepo.findById(niveauId)
                    .orElseThrow(() -> new ResourceNotFoundException("Niveau introuvable")));
        } else if (anneeId != null) {
            classes = classeRepo.findByAnneeAcademique(anneeRepo.findById(anneeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Année académique introuvable")));
        } else {
            classes = classeRepo.findAll();
        }
        return ResponseEntity.ok(classes.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<DTOs.ClasseDTO> create(@Valid @RequestBody DTOs.CreateClasseRequest req) {
        Niveau niveau = niveauRepo.findById(req.getNiveauId())
                .orElseThrow(() -> new ResourceNotFoundException("Niveau introuvable"));
        AnneeAcademique annee = anneeRepo.findById(req.getAnneeAcademiqueId())
                .orElseThrow(() -> new ResourceNotFoundException("Année académique introuvable"));
        Classe c = classeRepo.save(Classe.builder().nom(req.getNom()).niveau(niveau).anneeAcademique(annee).build());
        return ResponseEntity.status(201).body(toDTO(c));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        classeRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private DTOs.ClasseDTO toDTO(Classe c) {
        return DTOs.ClasseDTO.builder().id(c.getId()).nom(c.getNom())
                .niveauLibelle(c.getNiveau().getLibelle())
                .anneeLibelle(c.getAnneeAcademique().getLibelle()).build();
    }
}

// ══════════════════════════════════════════════════════════════════════════
// SEMESTRE
// ══════════════════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/admin/semestres")
@PreAuthorize("hasRole('ADMIN')")
class SemestreController {

    private final SemestreRepository semestreRepo;
    private final NiveauRepository niveauRepo;
    private final AnneeAcademiqueRepository anneeRepo;

    public SemestreController(SemestreRepository semestreRepo, NiveauRepository niveauRepo, AnneeAcademiqueRepository anneeRepo) {
        this.semestreRepo = semestreRepo;
        this.niveauRepo = niveauRepo;
        this.anneeRepo = anneeRepo;
    }

    @GetMapping
    public ResponseEntity<List<DTOs.SemestreDTO>> list(@RequestParam Long niveauId, @RequestParam Long anneeId) {
        Niveau niveau = niveauRepo.findById(niveauId)
                .orElseThrow(() -> new ResourceNotFoundException("Niveau introuvable"));
        AnneeAcademique annee = anneeRepo.findById(anneeId)
                .orElseThrow(() -> new ResourceNotFoundException("Année académique introuvable"));
        return ResponseEntity.ok(semestreRepo.findByNiveauAndAnneeAcademique(niveau, annee)
                .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<DTOs.SemestreDTO> create(@Valid @RequestBody DTOs.CreateSemestreRequest req) {
        Niveau niveau = niveauRepo.findById(req.getNiveauId())
                .orElseThrow(() -> new ResourceNotFoundException("Niveau introuvable"));
        AnneeAcademique annee = anneeRepo.findById(req.getAnneeAcademiqueId())
                .orElseThrow(() -> new ResourceNotFoundException("Année académique introuvable"));
        Semestre s = semestreRepo.save(Semestre.builder()
                .numero(req.getNumero()).niveau(niveau).anneeAcademique(annee).build());
        return ResponseEntity.status(201).body(toDTO(s));
    }

    private DTOs.SemestreDTO toDTO(Semestre s) {
        return DTOs.SemestreDTO.builder().id(s.getId()).numero(s.getNumero())
                .niveauLibelle(s.getNiveau().getLibelle())
                .anneeLibelle(s.getAnneeAcademique().getLibelle()).build();
    }
}

// ══════════════════════════════════════════════════════════════════════════
// UE (Unité d'Enseignement)
// ══════════════════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/admin/ues")
@PreAuthorize("hasRole('ADMIN')")
class UeController {

    private final UERepository ueRepo;
    private final SemestreRepository semestreRepo;

    public UeController(UERepository ueRepo, SemestreRepository semestreRepo) {
        this.ueRepo = ueRepo;
        this.semestreRepo = semestreRepo;
    }

    @GetMapping
    public ResponseEntity<List<DTOs.UeDTO>> list(@RequestParam Long semestreId) {
        Semestre semestre = semestreRepo.findById(semestreId)
                .orElseThrow(() -> new ResourceNotFoundException("Semestre introuvable"));
        return ResponseEntity.ok(ueRepo.findBySemestre(semestre).stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<DTOs.UeDTO> create(@Valid @RequestBody DTOs.CreateUeRequest req) {
        Semestre semestre = semestreRepo.findById(req.getSemestreId())
                .orElseThrow(() -> new ResourceNotFoundException("Semestre introuvable"));
        UE ue = ueRepo.save(UE.builder()
                .nom(req.getNom()).credits(req.getCredits())
                .seuilValidation(req.getSeuilValidation()).semestre(semestre).build());
        return ResponseEntity.status(201).body(toDTO(ue));
    }

    private DTOs.UeDTO toDTO(UE ue) {
        return DTOs.UeDTO.builder().id(ue.getId()).nom(ue.getNom()).credits(ue.getCredits())
                .seuilValidation(ue.getSeuilValidation()).semestreLabel(ue.getSemestre().toString()).build();
    }
}

// ══════════════════════════════════════════════════════════════════════════
// MATIÈRE (+ affectation d'enseignant)
// ══════════════════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/admin/matieres")
@PreAuthorize("hasRole('ADMIN')")
class MatiereController {

    private final MatiereRepository matiereRepo;
    private final UERepository ueRepo;
    private final UserRepository userRepo;
    private final AnneeAcademiqueRepository anneeRepo;
    private final AffectationEnseignantRepository affectationRepo;

    public MatiereController(MatiereRepository matiereRepo, UERepository ueRepo, UserRepository userRepo, AnneeAcademiqueRepository anneeRepo, AffectationEnseignantRepository affectationRepo) {
        this.matiereRepo = matiereRepo;
        this.ueRepo = ueRepo;
        this.userRepo = userRepo;
        this.anneeRepo = anneeRepo;
        this.affectationRepo = affectationRepo;
    }

    @GetMapping
    public ResponseEntity<List<DTOs.MatiereDTO>> list(@RequestParam Long ueId) {
        UE ue = ueRepo.findById(ueId).orElseThrow(() -> new ResourceNotFoundException("UE introuvable"));
        return ResponseEntity.ok(matiereRepo.findByUe(ue).stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<DTOs.MatiereDTO> create(@Valid @RequestBody DTOs.CreateMatiereRequest req) {
        UE ue = ueRepo.findById(req.getUeId()).orElseThrow(() -> new ResourceNotFoundException("UE introuvable"));
        Matiere m = matiereRepo.save(Matiere.builder()
                .nom(req.getNom()).coefficient(req.getCoefficient()).ue(ue).build());
        return ResponseEntity.status(201).body(toDTO(m));
    }

    /** v3 : remplace l'ancien lien direct Matiere.enseignant — permet la co-intervention */
    @PostMapping("/{id}/affecter-enseignant")
    public ResponseEntity<Void> affecterEnseignant(@PathVariable Long id,
                                                     @Valid @RequestBody DTOs.AffecterEnseignantRequest req) {
        Matiere matiere = matiereRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Matière introuvable"));
        User enseignant = userRepo.findById(req.getEnseignantId())
                .orElseThrow(() -> new ResourceNotFoundException("Enseignant introuvable"));
        if (!enseignant.isEnseignant())
            throw new BadRequestException("Cet utilisateur n'a pas le rôle Enseignant.");
        AnneeAcademique annee = anneeRepo.findById(req.getAnneeAcademiqueId())
                .orElseThrow(() -> new ResourceNotFoundException("Année académique introuvable"));
        if (affectationRepo.existsByEnseignantAndMatiere(enseignant, matiere))
            throw new BadRequestException("Cet enseignant est déjà affecté à cette matière.");
        affectationRepo.save(AffectationEnseignant.builder()
                .enseignant(enseignant).matiere(matiere).anneeAcademique(annee).build());
        return ResponseEntity.status(201).build();
    }

    private DTOs.MatiereDTO toDTO(Matiere m) {
        List<String> enseignants = m.getAffectations() == null ? List.of()
                : m.getAffectations().stream().map(a -> a.getEnseignant().getFullName()).collect(Collectors.toList());
        return DTOs.MatiereDTO.builder().id(m.getId()).nom(m.getNom()).coefficient(m.getCoefficient())
                .ueNom(m.getUe().getNom()).enseignants(enseignants).build();
    }
}

// ══════════════════════════════════════════════════════════════════════════
// STATISTIQUES — vue d'ensemble Admin (effectifs, moyennes, activité)
// ══════════════════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/admin/statistiques")
@PreAuthorize("hasRole('ADMIN')")
class AdminStatsController {

    private final com.unz.eval.service.AdminStatsService adminStatsService;

    public AdminStatsController(com.unz.eval.service.AdminStatsService adminStatsService) {
        this.adminStatsService = adminStatsService;
    }

    @GetMapping
    public ResponseEntity<DTOs.AdminStatsDTO> get() {
        return ResponseEntity.ok(adminStatsService.getStats());
    }
}

// ══════════════════════════════════════════════════════════════════════════
// INSCRIPTION — rattachement annuel étudiant ↔ classe (passage d'année)
