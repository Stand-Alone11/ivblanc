import React, { useState } from 'react';
import axios from 'axios';
import { Button } from 'react-bootstrap';

function MyStyleCreateModalBody({ saveClothesId }) {
  const [selectedImg, setSelectedImg] = useState();
  const imgHandleChange = (e) => {
    // console.log(e.target.files)
    setSelectedImg(e.target.files[0]);
  };

  const createClothes = () => {
    const formData = new FormData();
    formData.append('photo', selectedImg);

    axios
      .post('http://i6d104.p.ssafy.io:9999/api/clothes/add', formData, {
        headers: {
          'X-AUTH-TOKEN':
            'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZXMiOlsiUk9MRV9VU0VSIl0sInVzZXJQayI6IjEiLCJpYXQiOjE2NDM4Nzg4OTMsImV4cCI6MTY0NjQ3MDg5M30.Q2T5EQ38F53h1x037StKPwE-DBeqU0hBEAPY3D9w6WY',
        },
      })
      .then((response) => {
        console.log(response);
      })
      .catch((err) => {
        console.log('실패');
      });
  };

  return (
    <div>
      <div>
        <h2>스타일 등록</h2>
      </div>
      <input
        type='file'
        id='inputImg'
        accept='image/*'
        onChange={imgHandleChange}
      ></input>
      <div style={{ marginTop: '30px', float: 'right' }}>
        <Button variant='primary' onClick={createClothes}>
          등록
        </Button>
      </div>
    </div>
  );
}

export default MyStyleCreateModalBody;