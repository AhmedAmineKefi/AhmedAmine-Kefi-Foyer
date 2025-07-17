package tn.esprit.spring.RestControllers;

import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.DAO.Entities.Bloc;
import tn.esprit.spring.Services.Bloc.IBlocService;

import java.util.List;

@RestController
@RequestMapping("bloc")
@AllArgsConstructor
public class BlocRestController {
    IBlocService service;
    
    @Autowired
    private Tracer tracer;
    
    @Autowired
    private LongCounter blocCounter;

    @PostMapping("addOrUpdate")
    Bloc addOrUpdate(@RequestBody Bloc b) {
        Span span = tracer.spanBuilder("bloc.addOrUpdate").startSpan();
        try {
            blocCounter.add(1);
            return service.addOrUpdate(b);
        } finally {
            span.end();
        }
    }

    @GetMapping("findAll")
    List<Bloc> findAll() {
        Span span = tracer.spanBuilder("bloc.findAll").startSpan();
        try {
            blocCounter.add(1);
            return service.findAll();
        } finally {
            span.end();
        }
    }

    @GetMapping("findById")
    Bloc findById(@RequestParam long id) {
        Span span = tracer.spanBuilder("bloc.findById").startSpan();
        try {
            blocCounter.add(1);
            return service.findById(id);
        } finally {
            span.end();
        }
    }

    @DeleteMapping("delete")
    void delete(@RequestBody Bloc b) {
        Span span = tracer.spanBuilder("bloc.delete").startSpan();
        try {
            blocCounter.add(1);
            service.delete(b);
        } finally {
            span.end();
        }
    }

    @DeleteMapping("deleteById")
    void deleteById(@RequestParam long id) {
        Span span = tracer.spanBuilder("bloc.deleteById").startSpan();
        try {
            blocCounter.add(1);
            service.deleteById(id);
        } finally {
            span.end();
        }
    }

    @PutMapping("affecterChambresABloc")
    Bloc affecterChambresABloc(@RequestBody List<Long> numChambre, @RequestParam String nomBloc) {
        Span span = tracer.spanBuilder("bloc.affecterChambresABloc").startSpan();
        try {
            blocCounter.add(1);
            return service.affecterChambresABloc(numChambre, nomBloc);
        } finally {
            span.end();
        }
    }
    // ...............?nomFoyer=....&nomBloc=....
    @PutMapping("affecterBlocAFoyer")
    Bloc affecterBlocAFoyer(@RequestParam String nomBloc, @RequestParam String nomFoyer) {
        Span span = tracer.spanBuilder("bloc.affecterBlocAFoyer").startSpan();
        try {
            blocCounter.add(1);
            return service.affecterBlocAFoyer(nomBloc, nomFoyer);
        } finally {
            span.end();
        }
    }

    // .............../Foyer des jasmins/Bloc G
    @PutMapping("affecterBlocAFoyer2/{nomFoyer}/{nomBloc}")
    Bloc affecterBlocAFoyer2(@PathVariable String nomBloc, @PathVariable String nomFoyer) {
        Span span = tracer.spanBuilder("bloc.affecterBlocAFoyer2").startSpan();
        try {
            blocCounter.add(1);
            return service.affecterBlocAFoyer(nomBloc, nomFoyer);
        } finally {
            span.end();
        }
    }

    @PostMapping("ajouterBlocEtSesChambres")
    Bloc ajouterBlocEtSesChambres(@RequestBody Bloc b) {
        Span span = tracer.spanBuilder("bloc.ajouterBlocEtSesChambres").startSpan();
        try {
            blocCounter.add(1);
            return service.ajouterBlocEtSesChambres(b);
        } finally {
            span.end();
        }
    }

    @PostMapping("ajouterBlocEtAffecterAFoyer/{nomF}")
    Bloc ajouterBlocEtAffecterAFoyer(@RequestBody Bloc b,@PathVariable String nomF) {
        Span span = tracer.spanBuilder("bloc.ajouterBlocEtAffecterAFoyer").startSpan();
        try {
            blocCounter.add(1);
            return service.ajouterBlocEtAffecterAFoyer(b,nomF);
        } finally {
            span.end();
        }
    }
}
