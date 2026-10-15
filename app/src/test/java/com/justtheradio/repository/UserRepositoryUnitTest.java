package com.justtheradio.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;

import com.justtheradio.model.Result;
import com.justtheradio.source.user.BaseUserPreferencesLocalDataSource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class UserRepositoryUnitTest {

    @Rule // Makes LiveData run synchronously in tests
    public InstantTaskExecutorRule executorRule = new InstantTaskExecutorRule();

    private BaseUserPreferencesLocalDataSource userPreferencesLocalDataSource;
    private UserRepository userRepository;

    @Before
    public void setup() {
        // Mock the local data source in order to test only the repository
        userPreferencesLocalDataSource = mock(BaseUserPreferencesLocalDataSource.class);
        userRepository = new UserRepository(userPreferencesLocalDataSource);
    }

    @Test
    public void constructor_registersItselfAsCallback() {
        verify(userPreferencesLocalDataSource).setUserCallback(userRepository);
    }

    @Test
    public void onSavingUserPreferenceSuccess_emitsSuccess() {
        LiveData<Result> liveData = userRepository.saveUserPreference("country", "it");
        userRepository.onSavingUserPreferenceSuccess(); // Fire callback
        assertTrue(liveData.getValue() instanceof Result.Success);
    }

    @Test
    public void onRetrievingUserPreferenceSuccess_emitsSuccessWithResource() {
        LiveData<Result> liveData = userRepository.getUserPreference("country");
        userRepository.onRetrievingUserPreferenceSuccess("it");
        assertTrue(liveData.getValue() instanceof Result.Success);
        assertEquals("it", ((Result.Success<String>) liveData.getValue()).getData());
    }

    @Test
    public void onFailureSavingOnLocal_emitsFailure() {
        LiveData<Result> liveData = userRepository.saveUserPreference("country", "it");
        userRepository.onFailureSavingOnLocal(new Exception());
        assertTrue(liveData.getValue() instanceof Result.Failure);
    }

    @Test
    public void onFailureRetrievingOnLocal_emitsFailure() {
        LiveData<Result> liveData = userRepository.getUserPreference("country");
        userRepository.onFailureRetrievingOnLocal(new Exception());
        assertTrue(liveData.getValue() instanceof Result.Failure);
    }

    @Test
    public void saveAndGetLiveData_areIndependent() {
        LiveData<Result> saveData = userRepository.saveUserPreference("country", "it");
        LiveData<Result> getData = userRepository.getUserPreference("country");
        userRepository.onSavingUserPreferenceSuccess();
        assertTrue(saveData.getValue() instanceof Result.Success);
        assertNull(getData.getValue());
    }

}
