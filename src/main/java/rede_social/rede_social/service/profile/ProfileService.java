package rede_social.rede_social.service.profile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rede_social.rede_social.dto.profile.ProfileViewDTO;
import rede_social.rede_social.dto.profile.ResponseViewDTO;
import rede_social.rede_social.dto.profile.ViewDTO;
import rede_social.rede_social.model.ProfileView;
import rede_social.rede_social.model.User;
import rede_social.rede_social.repository.ProfileViewRepository;
import rede_social.rede_social.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

    @Autowired
    private ProfileViewRepository profileViewRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Registra uma visualização de perfil de um usuário em outro.
     *
     * Esse método é responsável por registrar quando um usuário (visualizador) visualiza o perfil de
     * outro usuário (dono do perfil). Ele verifica se o visualizador não é o mesmo que o dono do perfil,
     * busca os usuários no banco de dados e cria um registro de visualização.
     *
     * @param viewer O ID do usuário que está visualizando o perfil.
     * @param profileOwner O ID do usuário cujo perfil está sendo visualizado.
     * @return Retorna uma resposta com a mensagem de sucesso da operação.
     * @throws IllegalArgumentException Caso o visualizador seja o próprio dono do perfil ou algum dos usuários não seja encontrado.
     */
    @Transactional
    public ResponseEntity<ResponseViewDTO> viewProfile(Long viewer, Long profileOwner) {
        // Valida se o usuário não está tentando visualizar o próprio perfil
        if (viewer.equals(profileOwner)) {
            throw new IllegalArgumentException("Usuário não pode visualizar o próprio perfil");
        }

        // Encontra o visualizador e o dono do perfil no banco de dados
        User viewerUser = userRepository.findById(viewer).orElseThrow(() ->
                new IllegalArgumentException("Visualizador não encontrado"));
        User profileOwnerUser = userRepository.findById(profileOwner).orElseThrow(() ->
                new IllegalArgumentException("Dono do perfil não encontrado"));

        // Cria e salva o registro de visualização de perfil
        ProfileView profileView = new ProfileView();
        profileView.setViewer(viewerUser);
        profileView.setProfileOwner(profileOwnerUser);
        profileView.setViewedAt(LocalDateTime.now());
        profileViewRepository.save(profileView);

        // Registra a visualização no log
        logger.info("Usuário {} visualizou o perfil de {}", viewer, profileOwner);

        // Formata a mensagem de sucesso
        String message = String.format("Usuário %d visualizou o perfil de %d", viewer, profileOwner);

        // Retorna a resposta com a mensagem de sucesso
        return ResponseEntity.ok(new ResponseViewDTO(message, viewer, profileOwner));
    }

    /**
     * Retorna as visualizações de um perfil, incluindo o nome e o ID dos visualizadores
     * nos últimos 30 dias.
     *
     * Esse método consulta as visualizações de um perfil específico realizadas nos últimos 30 dias
     * e retorna uma lista de visualizadores (nome e ID) juntamente com a quantidade total de visualizações
     * registradas nesse período.
     *
     * @param profileOwner O ID do usuário dono do perfil.
     * @return Retorna uma resposta com as visualizações e a contagem de visualizações.
     * @throws IllegalArgumentException Caso o dono do perfil não seja encontrado.
     */
    public ResponseEntity<ProfileViewDTO> getViewProfile(Long profileOwner) {

        // Encontra o usuário dono do perfil no banco de dados
        User profileOwnerUser = userRepository.findById(profileOwner).orElseThrow(() ->
                new IllegalArgumentException("Dono do perfil não encontrado"));

        // Calcula a data de 30 dias atrás
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // Busca as visualizações de perfil realizadas nos últimos 30 dias
        List<ProfileView> profileViews = profileViewRepository.findByProfileOwnerAndViewedAtAfter(profileOwnerUser, thirtyDaysAgo);

        // Mapeia as visualizações para o formato ViewDTO (nome e ID do visualizador)
        List<ViewDTO> viewers = profileViews.stream()
                .map(profileView -> new ViewDTO(profileView.getViewer().getName(), profileView.getViewer().getId()))
                .collect(Collectors.toList());

        // Conta o número de visualizações
        int countViews = viewers.size();

        // Cria o DTO de resposta com as visualizações e a contagem
        ProfileViewDTO response = new ProfileViewDTO(viewers, countViews, profileOwnerUser.getId());

        // Retorna a resposta com as visualizações e o número total de visualizações
        return ResponseEntity.ok(response);
    }

}
