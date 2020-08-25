package unsl.services;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import unsl.entities.User;
import unsl.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
//import org.springframework.cache.annotation.CachePut;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import unsl.config.CacheConfig;
import java.util.List;
import unsl.entities.Cuenta;

@Service
public class UserService {
    private static Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    UserRepository userRepository;

    RestTemplate restTemplate = new RestTemplate();


    @CacheEvict(value = CacheConfig.USERS_CACHE, allEntries = true)
    public User saveUser(User user) {
        user.setStatus(User.Status.ACTIVO);
        return userRepository.save(user);
    }

    @Cacheable(CacheConfig.USERS_CACHE)
    public List<User> getAll() {
        simulatedSlowService();
        LOGGER.info("Getting users from DB");

        return userRepository.findAll();
    }

    @Cacheable(CacheConfig.USERS_CACHE)
    public User getUser(Long userId) {
        simulatedSlowService();
        LOGGER.info("Getting user {} from DB", userId);

        return userRepository.findById(userId).orElse(null);
    }

    @Cacheable(CacheConfig.USERS_CACHE)
    public User findByDni(Long dni) {
        simulatedSlowService();
        LOGGER.info("Getting user with dni: {} from DB", dni);

        return userRepository.findByDni(dni);
    }


    @CacheEvict(value = CacheConfig.USERS_CACHE, allEntries = true)
    public User updateUser(User updatedUser){
        User user = userRepository.findById(updatedUser.getId()).orElse(null);
        
        if (user ==  null){
            return null;
        }
        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        return userRepository.save(user);
    }
    @CacheEvict(value = CacheConfig.USERS_CACHE, allEntries = true)
    public User deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);;
        if (user ==  null){
            return null;
        }
        user.setStatus(User.Status.BAJA);
        return userRepository.save(user);
    }
    
    private void simulatedSlowService(){
        try{
            Thread.sleep(3000L);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    @CacheEvict(value = CacheConfig.USERS_CACHE, allEntries = true)
    public void updateEstado(Long id)throws Exception{
        try{
             restTemplate.put("http://localhost:8081/cuenta/" + id,Cuenta.class);
        }catch(Exception e){
            throw new Exception(buildMessageError(e));
        }
    }
    private String buildMessageError(Exception e) {
        String msg = e.getMessage();
        if (e instanceof HttpClientErrorException) {
            msg = ((HttpClientErrorException) e).getResponseBodyAsString();
        } else if (e instanceof HttpServerErrorException) {
            msg =  ((HttpServerErrorException) e).getResponseBodyAsString();
        }
        return msg;
    }

}
