import * as React from 'react';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import CssBaseline from '@mui/material/CssBaseline';
import TextField from '@mui/material/TextField';
import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';
import Link from '@mui/material/Link';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import Typography from '@mui/material/Typography';
import Container from '@mui/material/Container';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import Radio from '@mui/material/Radio';
import RadioGroup from '@mui/material/RadioGroup';
import FormControl from '@mui/material/FormControl';
import FormLabel from '@mui/material/FormLabel';

import { useState, useEffect, useCallback } from 'react';

function Copyright(props) {
  return (
    <Typography variant='body2' color='text.secondary' align='center' {...props}>
      {'Copyright © '}
      <Link color='inherit' href='https://mui.com/'>
        Your Website
      </Link>{' '}
      {new Date().getFullYear()}
      {'.'}
    </Typography>
  );
}

const theme = createTheme();

export default function SignUp() {
  // 이메일, 비밀번호, 비밀번호 확인, 이름, 성별, 나이, 폰 번호 확인
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [name, setName] = useState('');
  const [gender, setGender] = useState('female');
  const [age, setAge] = useState(0);
  const [phoneNum, setphoneNum] = useState('');

  // 오류메시지 상태저장
  const [emailMessage, setEmailMessage] = useState('');
  const [passwordMessage, setPasswordMessage] = useState('');
  const [passwordConfirmMessage, setPasswordConfirmMessage] = useState('');
  const [nameMessage, setNameMessage] = useState('');
  const [ageMessage, setAgeMessage] = useState('');
  const [phoneNumMessage, setphoneNumMessage] = useState('');

  // 유효성 검사
  const [isEmail, setIsEmail] = useState(false);
  const [isPassword, setIsPassword] = useState(false);
  const [isPasswordConfirm, setIsPasswordConfirm] = useState(false);
  const [isName, setIsName] = useState(false);
  const [isAge, setIsAge] = useState(false);
  const [isPhoneNum, setIsPhoneNum] = useState(false);

  // 아마도 백엔드 통신일 것.
  // const router = useRouter();

  // const onSubmit = useCallback(
  //   async (e: React.FormEvent<HTMLFormElement>) => {
  //     e.preventDefault()
  //     try {
  //       await axios
  //         .post(REGISTER_USERS_URL, {
  //           username: name,
  //           password: password,
  //           email: email,
  //         })
  //         .then((res) => {
  //           console.log('response:', res)
  //           if (res.status === 200) {
  //             router.push('/sign_up/profile_start')
  //           }
  //         })
  //     } catch (err) {
  //       console.error(err)
  //     }
  //   },
  //   [email, name, password, router]
  // )

  // 이메일 형식 확인
  const onChangeEmail = useCallback((e) => {
    const emailRegex = /([\w-.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([\w-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\]?)$/;
    const emailCurrent = e.target.value;
    setEmail(emailCurrent);

    if (!emailRegex.test(emailCurrent)) {
      setEmailMessage('이메일 형식이 틀렸어요! 다시 확인해주세요');
      setIsEmail(false);
    } else {
      setEmailMessage('올바른 이메일 형식이에요 : )');
      setIsEmail(true);
    }
  }, []);

  // 비밀번호
  const onChangePassword = useCallback((e) => {
    const passwordRegex = /^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-9]).{8,16}$/;
    const passwordCurrent = e.target.value;
    setPassword(passwordCurrent);

    if (!passwordRegex.test(passwordCurrent)) {
      setPasswordMessage('숫자+영문자+특수문자 조합으로 8자리 이상 입력해주세요!');
      setIsPassword(false);
    } else {
      setPasswordMessage('안전한 비밀번호에요 : )');
      setIsPassword(true);
    }
  }, []);

  // 비밀번호 확인
  const onChangePasswordConfirm = useCallback(
    (e) => {
      const passwordConfirmCurrent = e.target.value;
      setPasswordConfirm(passwordConfirmCurrent);

      if (password === passwordConfirmCurrent) {
        setPasswordConfirmMessage('비밀번호를 똑같이 입력했어요 :)');
        setIsPasswordConfirm(true);
      } else {
        setPasswordConfirmMessage('비밀번호가 틀려요. 다시 확인해주세요.');
        setIsPasswordConfirm(false);
      }
    },
    [password]
  );

  // 이름 확인
  const onChangeName = useCallback((e) => {
    const nameCurrent = e.target.value;
    setName(nameCurrent);

    if (nameCurrent === '') {
      setNameMessage('이름을 입력해 주세요.');
      setIsName(false);
    } else {
      setNameMessage('입력 되었습니다 :)');
      setIsName(true);
    }
  }, []);

  const handleChange = (e) => {
    setGender(e.target.value);
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);

    if (email === '') alert('이메일을 입력해주세요');
    if (passwordConfirm === '') alert('비밀번호를 입력해주세요');
    if (name === '') alert('이름을 확인해주세요');
    if (age === '') alert('나이를 입력해주세요');
    if (phoneNum === '') alert('전화번호를 입력해주세요');

    // eslint-disable-next-line no-console
    console.log({
      email: data.get('email'),
      password: data.get('password'),
      name: data.get('name'),
      gender: data.get('gender'),
      age: data.get('age'),
      phoneNum: data.get('phoneNum'),
    });
  };

  return (
    <ThemeProvider theme={theme}>
      <Container component='main' maxWidth='xs'>
        <CssBaseline />
        <Box
          sx={{
            marginTop: 8,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
          }}
        >
          <Avatar sx={{ m: 1, bgcolor: 'secondary.main' }}>
            <LockOutlinedIcon />
          </Avatar>
          <Typography component='h1' variant='h5'>
            Sign up
          </Typography>
          <Box component='form' noValidate onSubmit={handleSubmit} sx={{ mt: 3 }}>
            <Grid container spacing={2}>
              {/* <Grid item xs={12} sm={6}>
                <TextField autoComplete='given-name' name='firstName' required fullWidth id='firstName' label='First Name' autoFocus />
              </Grid> */}

              <Grid item xs={12}>
                <TextField required fullWidth id='email' label='이메일' name='email' autoComplete='email' value={email} onChange={onChangeEmail} />
                {email.length > 0 && <span className={`message ${isEmail ? 'success' : 'error'}`}>{emailMessage}</span>}
              </Grid>
              <Grid item xs={12}>
                <TextField required fullWidth name='password' label='비밀번호' type='password' id='password' autoComplete='new-password' onChange={onChangePassword} />
                {password.length > 0 && <span className={`message ${isPassword ? 'success' : 'error'}`}>{passwordMessage}</span>}
              </Grid>
              <Grid item xs={12}>
                <TextField required fullWidth name='password' label='비밀번호 확인' type='password' id='password' autoComplete='new-password' onChange={onChangePasswordConfirm} />
                {passwordConfirm.length > 0 && <span className={`message ${isPasswordConfirm ? 'success' : 'error'}`}>{passwordConfirmMessage}</span>}
              </Grid>
              <Grid item xs={12}>
                <TextField required fullWidth id='name' label='이름' name='name' autoComplete='family-name' onChange={onChangeName} />
                {name.length === 0 && <span className={`message ${isName ? 'success' : 'error'}`}>{nameMessage}</span>}
              </Grid>
              <Grid item xs={12} align='left'>
                <FormLabel id='demo-row-radio-buttons-group-label'>성별</FormLabel>
                <RadioGroup row aria-labelledby='demo-row-radio-buttons-group-label' name='gender' value={gender} onChange={handleChange}>
                  <FormControlLabel value='female' control={<Radio />} label='Female' />
                  <FormControlLabel value='male' control={<Radio />} label='Male' />
                </RadioGroup>
              </Grid>
              <Grid item xs={12}>
                <TextField required fullWidth id='age' label='나이' name='age' autoComplete='age' />
              </Grid>
              <Grid item xs={12}>
                <TextField required fullWidth id='phoneNum' label='전화번호' name='phoneNum' autoComplete='phoneNumber' />
              </Grid>
            </Grid>
            <Button type='submit' fullWidth variant='contained' sx={{ mt: 3, mb: 2 }}>
              회원가입
            </Button>
            <Grid container justifyContent='flex-end'>
              <Grid item>
                <Link href='#' variant='body2'>
                  Already have an account? Sign in
                </Link>
              </Grid>
            </Grid>
          </Box>
        </Box>
        <Copyright sx={{ mt: 5 }} />
      </Container>
    </ThemeProvider>
  );
}
