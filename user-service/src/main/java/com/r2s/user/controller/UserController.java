package com.r2s.user.controller;

import com.r2s.core.response.ApiResponse;
import com.r2s.core.response.ResponseBuilder;
import com.r2s.user.dto.request.UpdateUserRequest;
import com.r2s.user.dto.request.UserRequest;
import com.r2s.core.response.UserResponse;
import com.r2s.user.service.UserManagementService;
import com.r2s.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserManagementService userManagementService;
    private final UserProfileService userProfileService;
    private final ResponseBuilder responseBuilder;


    /**
     * Get all users (Admin only)
     * @return List of all users
     */
    @Operation(summary = "Get all users (Admin only)", description = "Retrieve a list of all registered users")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved list of users"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        log.info("Retrieving all users");
        List<UserResponse> users = userManagementService.getAllUsers();

        return responseBuilder.buildSuccessResponse(users, "Retrieved all users successfully");
    }

    /**
     * Get current user's profile
     * @param authentication Current user's authentication
     * @return User profile
     */
    @Operation(summary = "Get current user's profile")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved user profile"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(Authentication authentication) {
        log.info("Retrieving profile for user: {}", authentication.getName());

        String username = authentication.getName();
        UserResponse userResponse = userProfileService.getUserByUsername(username);

        return responseBuilder.buildSuccessResponse(userResponse, "Retrieved user profile successfully");
    }

    /**
     * Update current user's profile
     * @param updateUserRequest Update request
     * @param authentication Current user's authentication
     * @return Updated user profile
     */
    @Operation(summary = "Update current user's profile")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Updated user profile successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequest updateUserRequest
    ) {
        log.info("Updating profile for user: {}", authentication.getName());

        String username = authentication.getName();
        UserResponse updatedUser = userProfileService.updateUser(username, updateUserRequest);

        return responseBuilder.buildSuccessResponse(updatedUser, "Updated user profile successfully");
    }

    /**
     * Create a new user
     * @param userRequest User creation request
     * @return Created user response
     */
    @Operation(summary = "Create a new user (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User created successfully"), // Code 200 vì bạn dùng responseBuilder bọc lại
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest userRequest) {
        log.info("Creating new user with username: {}", userRequest.username());
        UserResponse createdUser = userManagementService.createUser(userRequest);

        return responseBuilder.buildSuccessResponse(createdUser, "User created successfully");
    }

    /**
     * Delete a user (Admin only)
     * @param username Username of user to delete
     * @return No content response
     */
    @Operation(summary = "Delete a user (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        log.info("Deleting user: {}", username);

        userManagementService.deleteUser(username);

        return responseBuilder.buildSuccessResponse(null, "User deleted successfully");
    }
}
