package rede_social.rede_social.dto.profile;

import rede_social.rede_social.model.ProfileView;

import java.util.List;

public record ProfileViewDTO(List<ViewDTO> viewers, int countViews, Long profileOwnerId) {
}
