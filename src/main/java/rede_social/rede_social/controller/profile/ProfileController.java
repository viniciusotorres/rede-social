package rede_social.rede_social.controller.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rede_social.rede_social.dto.profile.ProfileViewDTO;
import rede_social.rede_social.dto.profile.ResponseViewDTO;
import rede_social.rede_social.dto.profile.ViewProfileDTO;
import rede_social.rede_social.service.profile.ProfileService;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    /**
     * Registra uma visualização de perfil de um usuário em outro.
     *
     * Esse método permite que um usuário visualize o perfil de outro. O ID do visualizador e do dono do
     * perfil são passados via o corpo da requisição, e o serviço registra essa visualização.
     *
     * @param viewPro DTO contendo os IDs do visualizador e do dono do perfil.
     * @return Retorna uma resposta com a mensagem de sucesso da operação de visualização.
     */
    @PostMapping("/view/{viewerId}/{profileOwnerId}")
    public ResponseEntity<ResponseViewDTO> viewProfile(@RequestBody ViewProfileDTO viewPro) {
        // Chama o serviço para registrar a visualização do perfil
        return profileService.viewProfile(viewPro.viewerId(), viewPro.profileOwnerId());
    }

    /**
     * Retorna as visualizações de perfil de um usuário específico.
     *
     * Esse método permite consultar as visualizações de perfil de um usuário, retornando uma lista dos
     * visualizadores (nome e ID) e a quantidade de visualizações registradas nos últimos 30 dias.
     *
     * @param profileOwnerId O ID do dono do perfil cujas visualizações serão consultadas.
     * @return Retorna uma resposta com a lista de visualizações e a quantidade de visualizadores.
     */
    @GetMapping("/view/{profileOwnerId}")
    public ResponseEntity<ProfileViewDTO> getViewProfileById(@PathVariable Long profileOwnerId) {
        // Chama o serviço para buscar as visualizações de perfil do usuário
        return profileService.getViewProfile(profileOwnerId);
    }

}
