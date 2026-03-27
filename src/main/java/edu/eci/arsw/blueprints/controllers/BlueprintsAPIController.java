package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.ApiResponse;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import io.swagger.v3.oas.annotations.Operation;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

    // GET /api/v1/blueprints
    @Operation(summary = "Obtener todos los planos", description = "Retorna todos los blueprints registrados")
    @GetMapping
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        Set<Blueprint> blueprints = services.getAllBlueprints();
        ApiResponse<Set<Blueprint>> body = new ApiResponse<>(HttpStatus.OK.value(), "execute ok", blueprints);
        return ResponseEntity.ok(body);
    }

    // GET /api/v1/blueprints/{author}
    @Operation(summary = "Obtener planos por autor", description = "Retorna todos los blueprints de un autor dado")
    @GetMapping("/{author}")
    public ResponseEntity<ApiResponse<?>> byAuthor(@PathVariable String author) {
        try {
            Set<Blueprint> blueprints = services.getBlueprintsByAuthor(author);
            ApiResponse<Set<Blueprint>> body = new ApiResponse<>(HttpStatus.OK.value(), "execute ok", blueprints);
            return ResponseEntity.ok(body);
        } catch (BlueprintNotFoundException e) {
            ApiResponse<Void> error = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // GET /api/v1/blueprints/{author}/{bpname}
    @Operation(summary = "Obtener un plano específico", description = "Retorna un blueprint por autor y nombre")
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<ApiResponse<?>> byAuthorAndName(@PathVariable String author, @PathVariable String bpname) {
        try {
            Blueprint bp = services.getBlueprint(author, bpname);
            ApiResponse<Blueprint> body = new ApiResponse<>(HttpStatus.OK.value(), "execute ok", bp);
            return ResponseEntity.ok(body);
        } catch (BlueprintNotFoundException e) {
            ApiResponse<Void> error = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // POST /api/v1/blueprints
    @Operation(summary = "Crear un nuevo plano", description = "Crea un nuevo blueprint a partir de los datos enviados")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            ApiResponse<Blueprint> body = new ApiResponse<>(HttpStatus.CREATED.value(), "Created", bp);
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (BlueprintPersistenceException e) {
            ApiResponse<Void> error = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // PUT /api/v1/blueprints/{author}/{bpname}/points
    @Operation(summary = "Agregar un punto a un plano", description = "Agrega un nuevo punto al blueprint especificado")
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<ApiResponse<?>> addPoint(@PathVariable String author, @PathVariable String bpname,
                                      @RequestBody Point p) {
        if (p == null) {
            ApiResponse<Void> error = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Point payload is required", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            ApiResponse<Void> body = new ApiResponse<>(HttpStatus.ACCEPTED.value(), "Accepted", null);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
        } catch (BlueprintNotFoundException e) {
            ApiResponse<Void> error = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid java.util.List<Point> points
    ) { }
}
