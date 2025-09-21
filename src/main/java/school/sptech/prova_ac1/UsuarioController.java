package school.sptech.prova_ac1;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> buscarTodos() {
        if(usuarioRepository.findAll().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Usuario> criar(@RequestBody Usuario usuario) {
        if (!usuarioRepository.findByEmailOrCpf(usuario.getEmail(),usuario.getCpf()).isEmpty()){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Usuario usuarioAdicionado = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Integer id) {
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findById(id);
        return usuarioEncontrado.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        if(usuarioRepository.findById(id).isEmpty()){
            return  ResponseEntity.notFound().build();
        }
        usuarioRepository.delete(usuarioRepository.findById(id).get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filtro-data")
    public ResponseEntity<List<Usuario>> buscarPorDataNascimento(@RequestParam LocalDate nascimento) {

        List<Usuario> usuarioEncontrado = usuarioRepository.findByDataNascimentoAfter(nascimento);
        if(usuarioEncontrado.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(usuarioEncontrado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizar(@PathVariable Integer id, @RequestBody Usuario usuario) {
        Optional<Usuario> usuarioEncontradoOpt = usuarioRepository.findById(id);

        if (usuarioEncontradoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuarioExistente = usuarioEncontradoOpt.get();

        // Checa conflito de email ou CPF com outros usuários
        List<Usuario> possiveisConflitos = usuarioRepository.findByEmailOrCpf(usuario.getEmail(), usuario.getCpf());
        for (Usuario u : possiveisConflitos) {
            if (!u.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }

        // Atualiza somente os campos desejados
        usuarioExistente.setNome(usuario.getNome());
        usuarioExistente.setEmail(usuario.getEmail());
        usuarioExistente.setCpf(usuario.getCpf());
        usuarioExistente.setDataNascimento(usuario.getDataNascimento());
        usuarioExistente.setSenha(usuario.getSenha()); // se aplicável

        Usuario usuarioSalvo = usuarioRepository.save(usuarioExistente); // isso fará UPDATE

        return ResponseEntity.ok(usuarioSalvo);
    }
}
